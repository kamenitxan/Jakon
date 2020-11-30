package cz.kamenitxan.jakon.core.database

import java.lang.reflect.ParameterizedType
import java.sql._

import com.zaxxer.hikari.HikariDataSource
import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.model._
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.SEQ
import org.intellij.lang.annotations.Language
import org.sqlite.SQLiteConfig

import scala.collection.mutable

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {

	val objects: mutable.ArrayBuffer[Class[_ <: JakonObject]] = mutable.ArrayBuffer[Class[_ <: JakonObject]]()
	private lazy val ds = {
		val tds = new HikariDataSource(DBInitializer.config)
		tds.setLeakDetectionThreshold(60 * 1000)
		tds
	}


	def addDao[T <: JakonObject](jobject: Class[T]): Unit = {
		objects += jobject
	}

	def getDaoClasses: mutable.ArrayBuffer[Class[_ <: JakonObject]] = objects

	def getConnection: Connection = {
		//TODO: single conn for request

		val conn = if (Settings.getDatabaseType == DatabaseType.SQLITE) {
			Class.forName(Settings.getDatabaseDriver)
			var connection: Connection = null
			try {
				val config = new SQLiteConfig()
				config.enforceForeignKeys(true)
				connection = DriverManager.getConnection(Settings.getDatabaseConnPath,config.toProperties);
			} catch {
				case ex: SQLException =>
					Logger.error("Failed to get SQLITE connection with foreign key support", ex)
					connection = ds.getConnection
			}
			return connection
		} else {
			ds.getConnection
		}
		conn
	}

	@Deprecated
	def getPreparedStatement(sql: String): PreparedStatement = {
		getConnection.prepareStatement(sql)
	}

	def execute(stmt: PreparedStatement): ResultSet = {
		stmt.executeQuery()
	}

	def execute(stmt: Statement, sql: String): ResultSet = {
		stmt.executeQuery(sql)
	}

	def select[T <: JakonObject](stmt: PreparedStatement, cls: Class[T]): List[QueryResult[T]] = {
		val rs = execute(stmt)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			EntityMapper.createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def select[T <: JakonObject](stmt: Statement, @Language("SQL") sql: String, cls: Class[T]): List[QueryResult[T]] = {
		val rs = execute(stmt, sql)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			EntityMapper.createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def selectSingle[T <: JakonObject](stmt: PreparedStatement, cls: Class[T]): QueryResult[T] = {
		val rs = execute(stmt)
		var res: QueryResult[T] = null
		if (rs.next()) {
			res = EntityMapper.createJakonObject(rs, cls)
		} else {
			res = new QueryResult[T](null)
		}
		stmt.close()
		res
	}

	def selectSingle[T <: JakonObject](stmt: Statement, @Language("SQL") sql: String, cls: Class[T]): QueryResult[T] = {
		val rs = execute(stmt, sql)
		val res: QueryResult[T] = if (rs.next()) {
			EntityMapper.createJakonObject(rs, cls)
		} else {
			new QueryResult[T](null)
		}
		stmt.close()
		res
	}

	def selectSingleDeep[T <: JakonObject](stmt: Statement, @Language("SQL") sql: String)(implicit conn: Connection, cls: Class[T]): T = {
		val res = selectSingle(stmt, sql, cls)
		if (res.foreignIds != null && res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				selectForeignEntity(fki, res)(implicitly, cls)
			})
		}
		res.entity
	}

	def selectSingleDeep[T <: JakonObject](stmt: PreparedStatement)(implicit conn: Connection, cls: Class[T]): T = {
		val res = selectSingle(stmt, cls)
		if (res.foreignIds != null && res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				if (fki.ids.size == 1 && res.entity.id == fki.ids.head) {
					fki.field.set(res.entity, res.entity)
				} else {
					fetchForeignObjects(Seq(res))
				}
			})
		}
		res.entity
	}

	private def selectForeignEntity[T <: JakonObject](fki: ForeignKeyInfo, res: QueryResult[T])(implicit conn: Connection, cls: Class[T]): Unit = {
		val cls = fki.field.getType
		val className = cls.getSimpleName
		val sql = s"SELECT * FROM $className c JOIN JakonObject ON c.id = JakonObject.id WHERE " + "c.id = ? OR " * (fki.ids.size - 1) + "c.id = ?"
		val stmt = conn.prepareStatement(sql)
		fki.ids.zipWithIndex.foreach(idi => {
			stmt.setInt(idi._2 + 1, idi._1)
		})
		val r = selectDeep(stmt)(implicitly, cls.asInstanceOf[Class[JakonObject]])
		stmt.close()
		if (fki.ids.size > 1) {
			fki.field.set(res.entity, r)
		} else {
			fki.field.set(res.entity, r.head)
		}
	}

	def selectDeep[T <: JakonObject](stmt: Statement, @Language("SQL") sql: String)(implicit conn: Connection, cls: Class[T]): List[T] = {
		val res = select(stmt, sql, cls)
		fetchForeignObjects(res)
		res.map(r => r.entity)
	}

	def selectDeep[T <: JakonObject](stmt: PreparedStatement)(implicit conn: Connection, cls: Class[T]): Seq[T] = {
		val res = select(stmt, cls)
		fetchForeignObjects(res)
		res.map(r => r.entity)
	}

	def fetchForeignObjects[T <: JakonObject](resultList: Seq[QueryResult[T]])(implicit conn: Connection): Seq[QueryResult[T]] = {
		resultList.foreach(r => {
			if (r.foreignIds.nonEmpty) {
				r.foreignIds.foreach(fki => {
					val field = fki._2.field
					if (fki._2.ids.size == 1 && r.entity.id == fki._2.ids.head) {
						field.set(r.entity, r.entity)
					} else {
						//val objectClass = field.getType.asInstanceOf[Class[JakonObject]]
						val objectClass = field.getGenericType match {
							case parameterizedType: ParameterizedType =>
								Class.forName(parameterizedType.getActualTypeArguments.toList.head.getTypeName).asInstanceOf[Class[JakonObject]]
							case _ =>
								field.getType.asInstanceOf[Class[JakonObject]]
						}
						val className = field.getGenericType match {
							case parameterizedType: ParameterizedType =>
								val typeCls = parameterizedType.getActualTypeArguments.head
								typeCls.getTypeName.substring(typeCls.getTypeName.lastIndexOf(".") + 1)
							case _ =>
								objectClass.getSimpleName
						}

						val sql = s"SELECT * FROM $className c JOIN JakonObject ON c.id = JakonObject.id WHERE " + "c.id = ? OR " * (fki._2.ids.size - 1) + "c.id = ?"
						val stmt = conn.prepareStatement(sql)
						fki._2.ids.zipWithIndex.foreach(idi => {
							stmt.setInt(idi._2 + 1, idi._1)
						})

						val res = selectDeep(stmt)(implicitly, objectClass)
						field.getType match {
							case SEQ => field.set(r.entity, res)
							case _ => field.set(r.entity, res.head)
						}
					}
				})
			}
		})
		resultList
	}

	def count(@Language("SQL") countSql: String)(implicit conn: Connection): Long = {
		val usrStmt = conn.createStatement()
		val rs = usrStmt.executeQuery(countSql)
		rs.next()
		val userCount = rs.getInt(1)
		usrStmt.close()
		userCount
	}

	def withDbConnection[T](fun: Connection => T): T = {
		val conn = DBHelper.getConnection
		try {
			fun.apply(conn)
		} finally {
			conn.close()
		}
	}
}
