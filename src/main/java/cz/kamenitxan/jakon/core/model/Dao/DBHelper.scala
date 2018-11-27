package cz.kamenitxan.jakon.core.model.Dao

import java.util.Properties
import java.util.logging.{Level, Logger}

import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.model._
import org.hibernate.{HibernateException, Session, SessionFactory}
import org.hibernate.cfg.Configuration

import scala.collection.mutable

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {
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

}