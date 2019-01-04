package cz.kamenitxan.jakon.core.model.Dao

import java.io.File
import java.sql._
import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.model._
import cz.kamenitxan.jakon.utils.Utils
import javax.persistence.ManyToOne
import org.hibernate.cfg.Configuration
import org.hibernate.{HibernateException, Session, SessionFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	private var concreteSessionFactory: SessionFactory = _
	val objects: mutable.Set[Class[_ <: JakonObject]] = scala.collection.mutable.Set[Class[_ <: JakonObject]]()


	val prop = new Properties()
	prop.setProperty("hibernate.connection.url", Settings.getDatabaseConnPath)
	prop.setProperty("hibernate.connection.username", Settings.getProperty(SettingValue.DB_USER))
	prop.setProperty("hibernate.connection.password", Settings.getProperty(SettingValue.DB_PASS))
	prop.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect")
	//prop.setProperty("hibernate.hbm2ddl.auto", "update")
	prop.setProperty("hibernate.c3p0.min_size", "1")
	prop.setProperty("hibernate.c3p0.max_size", "1")
	prop.setProperty("hibernate.show_sql", "false")
	prop.setProperty("hibernate.format_sql", "true")
	prop.setProperty("hibernate.enable_lazy_load_no_trans", "true")
	prop.setProperty("hibernate.current_session_context_class", "thread")
	//prop.setProperty("hibernate.connection.autocommit", "true")


	addDao(classOf[AclRule])
	addDao(classOf[JakonUser])

	val config = new HikariConfig
	config.setJdbcUrl(Settings.getDatabaseConnPath)
	config.setUsername(Settings.getProperty(SettingValue.DB_USER))
	config.setPassword(Settings.getProperty(SettingValue.DB_PASS))
	config.addDataSourceProperty("cachePrepStmts", "true")
	config.addDataSourceProperty("prepStmtCacheSize", "250")
	config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
	config.addDataSourceProperty("PRAGMA foreign_keys", "ON")

	val ds = new HikariDataSource(config)
	ds.setLeakDetectionThreshold(60 * 1000)

	def createSessionFactory(): Unit = {
		val conf = new Configuration().addProperties(prop)
		objects.foreach(o => conf.addAnnotatedClass(o))
		concreteSessionFactory = conf.buildSessionFactory()
	}

	def addDao[T <: JakonObject](jobject: Class[T]) {
		objects += jobject
	}

	def createTables(): Unit = {
		val dbobj = objects + classOf[JakonObject]
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
				val resource = this.getClass.getResource(s"/sql/$className.sql")
				if (resource != null) {
					val file = new File(resource.getFile)
					val bufferedSource = Source.fromFile(file)
					val sql = bufferedSource.getLines().mkString("\n")
					val stmt = conn.createStatement()
					stmt.execute(sql)
					stmt.close()
					bufferedSource.close
				} else {
					logger.error(s"Table definition for $className not found")
				}
			}

		}
		conn.close()
	}


	@throws[HibernateException]
	@Deprecated
	def getSession: Session = {
		try {
			val session = concreteSessionFactory.getCurrentSession
			session
		} catch {
			case e: HibernateException => e.printStackTrace()
				null //concreteSessionFactory.openSession
		}
	}

	def getDaoClasses: mutable.Set[Class[_ <: JakonObject]] = objects

	/**
	  * @param id      searched JakonObject id
	  * @param refresh if true, object is queried from DB. not cache
	  * @return JakonObject or null
	  */
	@deprecated def getObjectById(id: Integer, refresh: Boolean): JakonObject = ???

	/**
	  * @param id searched JakonObject id
	  * @return JakonObject or null
	  */
	def getObjectById(id: Integer): JakonObject = {
		getObjectById(id, refresh = false)
	}

	def getConnection: Connection = {
		//TODO: single conn for request
		val conn = ds.getConnection
		logger.info("Got DB connection - " + conn)
		conn
	}

	@Deprecated
	def getPreparedStatement(sql: String): PreparedStatement = {
		getConnection.prepareStatement(sql)
	}

	@Deprecated
	def getPreparedStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement = {
		getConnection.prepareStatement(sql, autoGeneratedKeys)
	}

	def execute(stmt: PreparedStatement): ResultSet = {
		stmt.executeQuery()
	}

	def execute(stmt: Statement, sql: String): ResultSet = {
		stmt.executeQuery(sql)
	}

	private val S = classOf[String]
	private val B = classOf[Boolean]
	private val I = classOf[Int]

	def createJakonObject(rs: ResultSet, cls: Class[_ <: JakonObject]): QueryResult = {
		val rsmd = rs.getMetaData
		val obj = cls.newInstance()
		var foreignIds = Map[String, ForeignKeyInfo]()
		val columnCount = rsmd.getColumnCount

		Iterator.from(1).takeWhile(i => i <= columnCount).foreach(i => {
			val columnName = rsmd.getColumnName(i)
			val fieldName = if (columnName.endsWith("_id")) {
				columnName.substring(0, columnName.length - 3)
			} else {
				columnName
			}
			val fieldRef = Utils.getFieldsUpTo(cls, classOf[Object]).find(f => f.getName.equals(fieldName))
			if (fieldRef.nonEmpty) {
				val field = fieldRef.get
				field.setAccessible(true)
				field.getType match {
					case S => field.set(obj, rs.getString(columnName))
					case B => field.set(obj, rs.getBoolean(columnName))
					case I => field.set(obj, rs.getInt(columnName))
					case _ => {
						val ann = field.getAnnotation(classOf[ManyToOne])
						if (ann != null) {
							foreignIds += (columnName -> new ForeignKeyInfo(rs.getInt(columnName), columnName, field))
						} else {
							logger.warn("Uknown data type on " + cls.getSimpleName + s".$fieldName")
						}
					}
				}

			}
		})
		new QueryResult(obj,foreignIds)
	}

	def select(stmt: PreparedStatement, cls: Class[_ <: JakonObject]): List[QueryResult] = {
		val rs = execute(stmt)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def select(stmt: Statement, sql: String, cls: Class[_ <: JakonObject]): List[QueryResult] = {
		val rs = execute(stmt, sql)
		val res = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			createJakonObject(rs, cls)
		}).toList
		stmt.close()
		res
	}

	def selectSingle(stmt: PreparedStatement, cls: Class[_ <: JakonObject]): QueryResult = {
		val rs = execute(stmt)
		var res: QueryResult = null
		if (rs.next()) {
			res = createJakonObject(rs, cls)
		} else {
			res = new QueryResult(null, null)
		}
		stmt.close()
		res
	}

	def selectSingle(stmt: Statement, sql: String, cls: Class[_ <: JakonObject]): QueryResult = {
		val rs = execute(stmt, sql)
		val res = createJakonObject(rs, cls)
		stmt.close()
		res
	}

	def selectSingleDeep(stmt: PreparedStatement, cls: Class[_ <: JakonObject]): JakonObject = {
		val res = selectSingle(stmt, cls)
		if (res.foreignIds.nonEmpty) {
			res.foreignIds.values.foreach(fki => {
				val cls = fki.field.getType
				val sql = "SELECT * FROM " + cls.getSimpleName + " WHERE id = ?"
				val stmt = getPreparedStatement(sql)
				stmt.setInt(1, fki.id)
				val r = selectSingleDeep(stmt, cls.asInstanceOf[Class[JakonObject]])
				stmt.close()
				fki.field.set(res.entity, r)
			})
		}
		res.entity
	}


}