package cz.kamenitxan.jakon.core.database

import java.beans.Transient
import java.io.{BufferedReader, InputStreamReader}
import java.sql.{Connection, SQLException}
import java.util.stream.Collectors

import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper.{getConnection, logger}
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.ManyToOne
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable

/**
  * Created by TPa on 17/09/2019.
  */
object DBInitializer {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

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
		implicit val conn: Connection = getConnection
		try {
			checkCharacterSet
			checkChilds
			checkCollumns
		} catch {
			case ex: Exception => logger.error("Exception occurred when checking DB consistency", ex)
		} finally {
			conn.close()
			logger.info("DB consistency check complete")
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
				logger.error(s"Child record not found for id: $id")
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
					  logger.error(s"Field ${jo.getSimpleName}.${f.getName} is not in DB")
				  }
			  })
		})
	}

	case class TableCollumnInfo(
	                             name: String,
	                             dataType: String,
	                           )

}
