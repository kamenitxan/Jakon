package cz.kamenitxan.jakon.core.model.Dao

import java.lang.reflect.Field
import java.sql.{Connection, PreparedStatement, ResultSet, ResultSetMetaData}
import java.util.Properties

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.model._
import cz.kamenitxan.jakon.utils.Utils
import javax.persistence.ManyToOne
import org.hibernate.{HibernateException, Session, SessionFactory}
import org.hibernate.cfg.Configuration
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

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
	prop.setProperty("hibernate.hbm2ddl.auto", "update")
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

	val ds = new HikariDataSource(config)

	def createSessionFactory(): Unit = {
		val conf = new Configuration().addProperties(prop)
		objects.foreach(o => conf.addAnnotatedClass(o))
		concreteSessionFactory = conf.buildSessionFactory()
	}

	def addDao[T <: JakonObject](jobject: Class[T]) {
		objects += jobject
	}

	val postDao = new AbstractHibernateDao[Post](classOf[Post])

	def getPostDao: AbstractHibernateDao[Post] = postDao

	val pageDao = new AbstractHibernateDao[Page](classOf[Page])

	def getPageDao: AbstractHibernateDao[Page] = pageDao

	val categoryDao = new AbstractHibernateDao[Category](classOf[Category])

	def getCategoryDao: AbstractHibernateDao[Category] = categoryDao

	val userDao = new AbstractHibernateDao[JakonUser](classOf[JakonUser])

	def getUserDao: AbstractHibernateDao[JakonUser] = userDao


	@throws[HibernateException]
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

	def getPreparedStatement(sql: String): PreparedStatement = {
		getConnection.prepareStatement(sql)
	}

	def execute(stmt: PreparedStatement): ResultSet = {
		stmt.executeQuery()
	}

	private val S = classOf[String]
	private val B = classOf[Boolean]
	private def createJakonObject(rs: ResultSet, rsmd: ResultSetMetaData, cls: Class[_ <: JakonObject]): QueryResult = {
		val obj = cls.newInstance()
		var foreignIds = Map[String, Int]()
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
					case _ => {
						val ann = field.getAnnotation(classOf[ManyToOne])
						if (ann != null) {
							foreignIds += (columnName -> rs.getInt(columnName))
						}
					}
				}

			}
		})
		new QueryResult(obj,foreignIds)
	}

	def select(stmt: PreparedStatement, cls: Class[_ <: JakonObject]): List[QueryResult] = {
		val rs = execute(stmt)
		val rsmd = rs.getMetaData
		Iterator.from(0).takeWhile(_ => rs.next()).map(_ => {
			createJakonObject(rs, rsmd, cls)
		}).toList
	}

	def selectSingle(stmt: PreparedStatement, cls: Class[_ <: JakonObject]): QueryResult = {
		val rs = execute(stmt)
		val rsmd = rs.getMetaData
		createJakonObject(rs, rsmd, cls)
	}


}