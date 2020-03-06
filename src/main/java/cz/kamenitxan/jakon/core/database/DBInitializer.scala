package cz.kamenitxan.jakon.core.database

import java.io.File
import java.sql.{Connection, SQLException}

import com.zaxxer.hikari.HikariConfig
import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper.getConnection
import cz.kamenitxan.jakon.core.model._
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{ManyToOne, Transient}

import scala.collection.mutable

/**
  * Created by TPa on 17/09/2019.
  */
object DBInitializer {

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

	def dbExists(): Unit = {
		if (Settings.getDatabaseType == DatabaseType.SQLITE) {
			val dbFile = Settings.getDatabaseConnPath.replace("jdbc:sqlite:", "")
			val exists = new File(dbFile).exists()
			if (!exists) {
				Logger.critical("SQLite DB file does not exist. Restart Jakon in DEVEL mode to create it.")
				System.exit(42)
			}
		}
	}

	def registerCoreObjects(): Unit = {
		DBHelper.addDao(classOf[AclRule])
		DBHelper.addDao(classOf[JakonUser])
		DBHelper.addDao(classOf[KeyValueEntity])
		DBHelper.addDao(classOf[JakonFile])
	}

	def createTables(): Unit = {
		val dbobj = mutable.ArrayBuffer[Class[_ <: JakonObject]]()
		DBHelper.objects.copyToBuffer(dbobj)
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
				Logger.debug(className + " found in DB")
			} else {
				Logger.info(className + " not found in DB")
				val resource = Utils.getResourceFromJar(s"/sql/$className.sql")
				if (resource.nonEmpty) {
					var sql = resource.get
					if (Settings.getDatabaseType == DatabaseType.SQLITE) {
						sql = sql.replaceAll("AUTO_INCREMENT", "")
					}
					val stmt = conn.createStatement()
					stmt.execute(sql)
					stmt.close()
				} else {
					Logger.error(s"Table definition for $className not found")
				}
			}

		}
		conn.close()
	}

	def checkDbConsistency(): Unit = {
		implicit val conn: Connection = getConnection
		try {
			checkCharacterSet
			checkChilds
			checkCollumns
		} catch {
			case ex: Exception => Logger.error("Exception occurred when checking DB consistency", ex)
		} finally {
			conn.close()
			Logger.info("DB consistency check complete")
		}
	}

	private def checkCharacterSet(implicit conn: Connection): Unit = {
		if (Settings.getDatabaseType == DatabaseType.MYSQL) {
			val characterSetSql = "SELECT @@character_set_database;"
			val stmt = conn.createStatement()
			val characterSet = stmt.executeQuery(characterSetSql)
			println(characterSet)
		}
	}

	private def checkChilds(implicit conn: Connection): Unit = {
		val joSql = "SELECT id, childClass FROM JakonObject"
		val stmt = conn.createStatement()
		val rs = stmt.executeQuery(joSql)
		val jakonObjects = Iterator.from(0).takeWhile(_ => rs.next()).map(_ => (rs.getInt(1), rs.getString(2))).toList
		jakonObjects.foreach(jo => {
			// check child objects
			val id = jo._1
			val tableName = jo._2.substring(jo._2.lastIndexOf(".") + 1)
			val sql = s"SELECT id FROM $tableName WHERE id = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, id)
			val rs = stmt.executeQuery()
			if (!rs.next()) {
				Logger.error(s"Child record not found for id: $id")
				val deleteSql = "DELETE FROM JakonObject WHERE id = ?"
				val delStmt = conn.prepareStatement(deleteSql)
				delStmt.setInt(1, id)
				delStmt.executeUpdate()
			}
		})
	}

	private def checkCollumns(implicit conn: Connection): Unit = {
		DBHelper.objects.foreach(jo => {
			val tableName = jo.getSimpleName
			val stmt = conn.createStatement()
			val collumns = Settings.getDatabaseType match {
				case DatabaseType.SQLITE =>
					val sql = s"PRAGMA table_info($tableName)"
					val rs = stmt.executeQuery(sql)
					Iterator.from(0).takeWhile(_ => rs.next()).map(_ => TableCollumnInfo(rs.getString(2), rs.getString(3))).toSeq
				case DatabaseType.MYSQL =>
					val sql = s"DESCRIBE $tableName"
					val rs = stmt.executeQuery(sql)
					Iterator.from(0).takeWhile(_ => rs.next()).map(_ => TableCollumnInfo(rs.getString(1), rs.getString(2))).toSeq
			}
			jo.getDeclaredFields
			  .filter(f => f.getAnnotation(classOf[JakonField]) != null && f.getAnnotation(classOf[Transient]) == null)
			  .foreach(f => {
				  val manyToOne = f.getAnnotation(classOf[ManyToOne])
				  val column = if (manyToOne != null) {
					  collumns.find(c => c.name == f.getName + "_id")
				  } else {
					  collumns.find(c => c.name == f.getName)
				  }
				  if (column.isEmpty) {
					  cz.kamenitxan.jakon.logging.Logger.error(s"Field ${jo.getSimpleName}.${f.getName} is not in DB")
				  }
			  })
		})
	}

	case class TableCollumnInfo(
	                             name: String,
	                             dataType: String,
	                           )

}
