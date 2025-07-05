package cz.kamenitxan.jakon.core.database

import com.zaxxer.hikari.HikariDataSource
import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.database.annotation.OneToMany
import cz.kamenitxan.jakon.core.model.*
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.SEQ
import cz.kamenitxan.jakon.utils.Utils.*
import org.sqlite.SQLiteConfig

import java.sql.*
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

	def execute(stmt: PreparedStatement): ResultSet = {
		stmt.executeQuery()
	}

	def execute(stmt: Statement, sql: String): ResultSet = {
		stmt.executeQuery(sql)
	}

	def select[T <: BaseEntity](stmt: PreparedStatement, cls: Class[T])(implicit conn: Connection): List[QueryResult[T]] = {
		val rs = execute(stmt)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			EntityMapper.createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res.foreach(r => fetchI18nData(r))
		res
	}

	def select[T <: BaseEntity](stmt: Statement, sql: String, cls: Class[T])(implicit conn: Connection): List[QueryResult[T]] = {
		val rs = execute(stmt, sql)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			EntityMapper.createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res.foreach(r => fetchI18nData(r))
		res
	}

	def selectSingle[T <: JakonObject](stmt: PreparedStatement, cls: Class[T])(implicit conn: Connection): QueryResult[T] = {
		val rs = execute(stmt)
		var res: QueryResult[T] = null
		if (rs.next()) {
			res = EntityMapper.createJakonObject(rs, cls)
		} else {
			res = new QueryResult[T](null)
		}
		stmt.close()
		fetchI18nData(res)
		res
	}

	def selectSingle[T <: JakonObject](stmt: Statement, sql: String, cls: Class[T])(implicit conn: Connection): QueryResult[T] = {
		val rs = execute(stmt, sql)
		val res: QueryResult[T] = if (rs.next()) {
			EntityMapper.createJakonObject(rs, cls)
		} else {
			new QueryResult[T](null)
		}
		stmt.close()
		fetchI18nData(res)
		res
	}

	def selectSingleDeep[T <: JakonObject](stmt: Statement, sql: String)(implicit conn: Connection, cls: Class[T]): Option[T] = {
		val res = selectSingle(stmt, sql, cls)
		if (res.foreignIds != null && res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				selectForeignEntity(fki, res)(implicitly, cls)
			})
		}
		Option(res.entity)
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

		fetchI18nData(res)
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

	def selectDeep[T <: JakonObject](stmt: Statement, sql: String)(implicit conn: Connection, cls: Class[T]): Seq[T] = {
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
						val oneToManyAnn = field.getDeclaredAnnotation(classOf[OneToMany])
						val objectClass = if (oneToManyAnn != null) {
							oneToManyAnn.genericClass().asInstanceOf[Class[JakonObject]]
						} else {
							field.getType.asInstanceOf[Class[JakonObject]]
						}

						val (className, superClass) = (objectClass.getSimpleName, objectClass.getSuperclass)
						val joinSql = if (objectClass.getSuperclass != classOf[JakonObject]) {
							s"JOIN ${superClass.getSimpleName} s ON s.id = JakonObject.id"
						} else {
							""
						}

						val sql = s"SELECT * FROM $className c JOIN JakonObject ON c.id = JakonObject.id $joinSql WHERE " + "c.id = ? OR " * (fki._2.ids.size - 1) + "c.id = ?"
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

	private def fetchI18nData[T <: BaseEntity](res: QueryResult[T])(implicit conn: Connection): Unit =  {
		if (res.i18nField.nonEmpty && res.entity.isInstanceOf[JakonObject]) {
			val i18nF = res.i18nField.get
			val cls = i18nF.getDeclaredAnnotation(classOf[I18n]).genericClass().asInstanceOf[Class[BaseEntity]]
			val sql = s"SELECT * FROM ${cls.getSimpleName} WHERE id = ?"
			val i18nStmt = conn.prepareStatement(sql)
			i18nStmt.setInt(1, res.entity.asInstanceOf[JakonObject].id)
			val i18nRes = select(i18nStmt, cls).map(_.entity)
			if (!i18nF.trySetAccessible()) {
				throw new IllegalStateException(s"Can´t set access for field ${i18nF.getName}")
			}

			i18nF.set(res.entity, i18nRes)
		}
	}

	def count(countSql: String)(implicit conn: Connection): Long = {
		val stmt = conn.createStatement()
		val rs = stmt.executeQuery(countSql)
		rs.next()
		val count = rs.getInt(1)
		stmt.close()
		count
	}

	def count(stmt: PreparedStatement)(implicit conn: Connection): Long = {
		val rs = stmt.executeQuery()
		rs.next()
		val count = rs.getInt(1)
		stmt.close()
		count
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
