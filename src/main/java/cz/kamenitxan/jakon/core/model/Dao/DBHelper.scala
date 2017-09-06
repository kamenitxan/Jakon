package cz.kamenitxan.jakon.core.model.Dao

import java.sql.SQLException
import java.util.Properties
import javax.persistence.TypedQuery
import javax.persistence.criteria.{CriteriaBuilder, CriteriaQuery, ParameterExpression, Root}

import cz.kamenitxan.jakon.core.Settings
import cz.kamenitxan.jakon.core.model._
import org.hibernate.HibernateException
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.reflect.ClassTag

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {
	private var concreteSessionFactory: SessionFactory = _
	val objects = scala.collection.mutable.Set[Class[_ <: JakonObject]]()


	val prop = new Properties()
	prop.setProperty("hibernate.connection.url", Settings.getDatabaseConnPath)
	prop.setProperty("hibernate.connection.username", Settings.getProperty("databaseUser"))
	prop.setProperty("hibernate.connection.password", Settings.getProperty("databasePass"))
	prop.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect")
	//prop.setProperty("hibernate.hbm2ddl.auto", "create")
	//prop.setProperty("hibernate.show_sql", "true")
  	//prop.setProperty("hibernate.format_sql", "true")



	addDao(classOf[JakonUser])

	def addDao[T <: JakonObject](jobject: Class[T]) {
		objects += jobject
		val conf = new Configuration().addProperties(prop)
		objects.foreach(o => conf.addAnnotatedClass(o))
		concreteSessionFactory = conf.buildSessionFactory()
	}

	val postDao = new AbstractHibernateDao[Post](classOf[Post])
	def getPostDao = postDao

	val pageDao = new AbstractHibernateDao[Page](classOf[Page])
	def getPageDao = pageDao

	val categoryDao = new AbstractHibernateDao[Category](classOf[Category])
	def getCategoryDao = categoryDao

	val userDao = new AbstractHibernateDao[JakonUser](classOf[JakonUser])
	def getUserDao = userDao


	@throws[HibernateException]
	def getSession: Session = concreteSessionFactory.openSession

	def getDaoClasses = objects

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