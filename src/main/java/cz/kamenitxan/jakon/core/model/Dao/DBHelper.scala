package cz.kamenitxan.jakon.core.model.Dao

import java.io.{BufferedReader, InputStreamReader}
import java.sql._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.model._
import cz.kamenitxan.jakon.core.model.converters.AbstractConverter
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{Column, ManyToOne}
import org.slf4j.{Logger, LoggerFactory}
import org.sqlite.SQLiteConfig

import scala.collection.mutable

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val objects: mutable.ArrayBuffer[Class[_ <: JakonObject]] = mutable.ArrayBuffer[Class[_ <: JakonObject]]()


	addDao(classOf[AclRule])
	addDao(classOf[JakonUser])
	addDao(classOf[KeyValueEntity])
	addDao(classOf[JakonFile])

	val config = new HikariConfig
	config.setJdbcUrl(Settings.getDatabaseConnPath)
	config.setUsername(Settings.getDatabaseUser)
	config.setPassword(Settings.getDatabasePass)
	config.addDataSourceProperty("cachePrepStmts", "true")
	config.addDataSourceProperty("prepStmtCacheSize", "250")
	config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
	if (Settings.getDatabaseType == DatabaseType.SQLITE) {
		config.addDataSourceProperty("PRAGMA foreign_keys", "ON")
		config.addDataSourceProperty("PRAGMA journal_mode", "wal")
	}
	val ds = new HikariDataSource(config)
	ds.setLeakDetectionThreshold(60 * 1000)


	def addDao[T <: JakonObject](jobject: Class[T]) {
		objects += jobject
	}

	def createTables(): Unit = {
		val dbobj = mutable.ArrayBuffer[Class[_ <: JakonObject]]()
		objects.copyToBuffer(dbobj)
		dbobj.+=:(classOf[JakonObject])
		val conn = getConnection
		for (o <- dbobj) {
			val className = o.getSimpleName
			val check = "SELECT 1 FROM " + className + " LIMIT 1"

			val stmt = conn.createStatement()
			var found = false
			try {
				stmt.executeQuery(check)
				found = true
			} catch {
				case _: SQLException =>
			}
			stmt.close()

			if (found) {
				logger.debug(className + " found in DB")
			} else {
				logger.info(className + " not found in DB")
				val resource = this.getClass.getResourceAsStream(s"/sql/$className.sql")
				if (resource != null) {
					var sql = new BufferedReader(new InputStreamReader(resource)).lines().collect(Collectors.joining("\n"))
					if (Settings.getDatabaseType == DatabaseType.SQLITE) {
						sql = sql.replaceAll("AUTO_INCREMENT", "")
					}
					val stmt = conn.createStatement()
					stmt.execute(sql)
					stmt.close()
				} else {
					logger.error(s"Table definition for $className not found")
				}
			}

		}
		conn.close()
	}

	def checkDbConsistency(): Unit = {
		val conn = getConnection
		try {
			if (Settings.getDatabaseType == DatabaseType.MYSQL) {
				val characterSetSql = "SELECT @@character_set_database;"
				val stmt = conn.createStatement()
				val characterSet = stmt.executeQuery(characterSetSql)
				println(characterSet)
			}
		} catch {
			case ex: Exception => logger.error("Exception occurred when checking DB consistency", ex)
		} finally {
			conn.close()
			logger.info("DB consistency check complete")
		}
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
					logger.error("Failed to get SQLITE connection with foreign key support")
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


	def createJakonObject[T <: JakonObject](rs: ResultSet, cls: Class[T]): QueryResult[T] = {
		val rsmd = rs.getMetaData
		val obj = cls.newInstance()
		var foreignIds = Map[String, ForeignKeyInfo]()
		val columnCount = rsmd.getColumnCount

		Iterator.from(1).takeWhile(i => i <= columnCount).foreach(i => {
			var columnName = rsmd.getColumnName(i)
			val fieldName = if (columnName.endsWith("_id")) {
				columnName.substring(0, columnName.length - 3)
			} else {
				columnName
			}


			val fieldRef = Utils.getFieldsUpTo(cls, classOf[Object]).find(f => {
				val byName = f.getName.equalsIgnoreCase(fieldName)
				lazy val byAnn = {
					val ann = f.getDeclaredAnnotation(classOf[Column])
					if (ann != null) {
						fieldName == ann.name()
					} else {
						false
					}
				}
				byName || byAnn
			})

			if (fieldRef.nonEmpty) {
				val field = fieldRef.get
				field.setAccessible(true)
				val columnAnn = field.getAnnotation(classOf[Column])
				if (columnAnn != null && columnAnn.name() != null && !columnAnn.name().isEmpty) {
					columnName = columnAnn.name()
				}
				field.getType match {
					case STRING => field.set(obj, rs.getString(columnName))
					case BOOLEAN => field.set(obj, rs.getBoolean(columnName))
					case INTEGER => field.set(obj, rs.getInt(columnName))
					case DOUBLE => field.set(obj, rs.getDouble(columnName))
					case DATE => field.set(obj, rs.getDate(columnName))
					case DATETIME => field.set(obj, LocalDateTime.parse(rs.getString(columnName), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
					case x if x.isEnum =>
						val m = x.getMethod("valueOf", classOf[String])
						val enumValue = m.invoke(null, rs.getString(columnName))
						field.set(obj, enumValue)
					case _ =>
						val manyToOne = field.getAnnotation(classOf[ManyToOne])
						lazy val jakonField = field.getAnnotation(classOf[JakonField])
						if (manyToOne != null) {
							val fv = rs.getInt(columnName)
							if (fv > 0) {
								foreignIds += (columnName -> new ForeignKeyInfo(rs.getInt(columnName), columnName, field))
							}
						} else if (jakonField != null) {
							val converter = jakonField.converter()
							if (converter.getName != classOf[AbstractConverter[_]].getName) {
								field.set(obj, converter.newInstance().convertToEntityAttribute(rs.getString(columnName)))
							} else {
								logger.error(s"Convertor not specified for data type on ${obj.getClass.getSimpleName}.${field.getName}")
							}
						} else {
							logger.warn("Uknown data type on " + cls.getSimpleName + s".$fieldName")
						}
				}

			}
		})
		new QueryResult(obj, foreignIds)
	}

	def select[T <: JakonObject](stmt: PreparedStatement, cls: Class[T]): List[QueryResult[T]] = {
		val rs = execute(stmt)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def select[T <: JakonObject](stmt: Statement, sql: String, cls: Class[T]): List[QueryResult[T]] = {
		val rs = execute(stmt, sql)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def selectSingle[T <: JakonObject](stmt: PreparedStatement, cls: Class[T]): QueryResult[T] = {
		val rs = execute(stmt)
		var res: QueryResult[T] = null
		if (rs.next()) {
			res = createJakonObject(rs, cls)
		} else {
			res = new QueryResult[T](null)
		}
		stmt.close()
		res
	}

	def selectSingle[T <: JakonObject](stmt: Statement, sql: String, cls: Class[T]): QueryResult[T] = {
		val rs = execute(stmt, sql)
		val res: QueryResult[T] = if (rs.next()) {
			createJakonObject(rs, cls)
		} else {
			new QueryResult[T](null)
		}
		stmt.close()
		res
	}

	def selectSingleDeep[T <: JakonObject](stmt: Statement, sql: String, cls: Class[T])(implicit conn: Connection): T = {
		val res = selectSingle(stmt, sql, cls)
		if (res.foreignIds != null && res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				val cls = fki.field.getType
				val sql = "SELECT * FROM " + cls.getSimpleName + " WHERE id = ?"
				val stmt = conn.prepareStatement(sql)
				stmt.setInt(1, fki.id)
				val r = selectSingleDeep(stmt, cls.asInstanceOf[Class[JakonObject]])
				stmt.close()
				fki.field.set(res.entity, r)
			})
		}
		res.entity
	}

	def selectSingleDeep[T <: JakonObject](stmt: PreparedStatement, cls: Class[T])(implicit conn: Connection): T = {
		val res = selectSingle(stmt, cls)
		if (res.foreignIds != null && res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				val cls = fki.field.getType
				val sql = "SELECT * FROM " + cls.getSimpleName + " WHERE id = ?"
				val stmt = conn.prepareStatement(sql)
				stmt.setInt(1, fki.id)
				val r = selectSingleDeep(stmt, cls.asInstanceOf[Class[JakonObject]])
				stmt.close()
				fki.field.set(res.entity, r)
			})
		}
		res.entity
	}

	def selectDeep[T <: JakonObject](stmt: Statement, sql: String, cls: Class[T])(implicit conn: Connection): List[T] = {
		val res = select(stmt, sql, cls)
		fetchForeignObjects(res)
		res.map(r => r.entity)
	}

	def selectDeep[T <: JakonObject](stmt: PreparedStatement, cls: Class[T])(implicit conn: Connection): List[T] = {
		val res: List[QueryResult[T]] = select(stmt, cls)
		fetchForeignObjects(res)
		res.map(r => r.entity)
	}

	def fetchForeignObjects[T <: JakonObject](resultList: List[QueryResult[T]])(implicit conn: Connection): List[QueryResult[T]] = {
		resultList.foreach(r => {
			if (r.foreignIds.nonEmpty) {
				r.foreignIds.foreach(fki => {
					val field = fki._2.field
					val objectClass = field.getType.asInstanceOf[Class[JakonObject]]
					val stmt = conn.prepareStatement(s"SELECT * FROM ${objectClass.getSimpleName} WHERE id = ?")
					stmt.setInt(1, fki._2.id)
					val res = selectSingleDeep(stmt, objectClass)
					field.set(r.entity, res)
				})
			}
		})
		resultList
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