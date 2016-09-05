package cz.kamenitxan.jakon.core.model.Dao

import java.sql.SQLException

import com.j256.ormlite.dao.{Dao, DaoManager, ReferenceObjectCache}
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource
import com.j256.ormlite.table.TableUtils
import cz.kamenitxan.jakon.core.Settings
import cz.kamenitxan.jakon.core.model._

import scala.collection.mutable

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 20.12.15.
  */
object DBHelper {
	private val daos = mutable.Map[Class[_ <: JakonObject], Dao[JakonObject, Integer]]()

	try {
		Class.forName(Settings.getDatabaseDriver)
	} catch {
		case e: ClassNotFoundException => e.printStackTrace()
	}
	addDao(classOf[JakonUser])

	def addDao[T <: JakonObject](jobject: Class[T]) {
		try {
			val connectionSource = new JdbcPooledConnectionSource(Settings.getDatabaseConnPath, Settings.getProperty("databaseUser"), Settings.getProperty("databasePass"))
			val dao: Dao[JakonObject, Integer] = DaoManager.createDao(connectionSource, jobject)
			dao.setObjectCache(ReferenceObjectCache.makeSoftCache)
			if (!dao.isTableExists) {
				TableUtils.createTable(connectionSource, jobject)
			}
			daos += (jobject -> dao)
		}
		catch {
			case e: SQLException => e.printStackTrace()
		}
	}

	def getDao(objectClass: Class[_ <: JakonObject]): Dao[JakonObject, Integer] = {
		daos.getOrElse[Dao[JakonObject, Integer]](objectClass, null)
	}

	def getPostDao = getDao(classOf[Post]).asInstanceOf[Dao[Post, Integer]]

	def getPageDao = getDao(classOf[Page]).asInstanceOf[Dao[Page, Integer]]

	def getCategoryDao = getDao(classOf[Category]).asInstanceOf[Dao[Category, Integer]]

	def getUserDao = getDao(classOf[JakonUser]).asInstanceOf[Dao[JakonUser, Integer]]

	/**
	  * @param id      searched JakonObject id
	  * @param refresh if true, object is queried from DB. not cache
	  * @return JakonObject or null
	  */
	@deprecated def getObjectById(id: Integer, refresh: Boolean): JakonObject = {
		try {
			for (dao <- daos.values) {
				val o: JakonObject = dao.queryForId(id)
				if (o != null) {
					return o
				}
			}
		} catch {
			case e: SQLException => e.printStackTrace()
		}
		null
	}

	/**
	  * @param id searched JakonObject id
	  * @return JakonObject or null
	  */
	def getObjectById(id: Integer): JakonObject = {
		getObjectById(id, refresh = false)
	}

}