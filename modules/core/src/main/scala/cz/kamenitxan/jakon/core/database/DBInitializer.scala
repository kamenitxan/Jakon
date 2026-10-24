package cz.kamenitxan.jakon.core.database

import com.zaxxer.hikari.HikariConfig
import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper.getConnection
import cz.kamenitxan.jakon.core.database.annotation.{Column, ManyToMany, ManyToOne, Transient}
import cz.kamenitxan.jakon.core.model.*
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils

import java.io.File
import java.sql.{Connection, SQLException}
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
			val file = new File(dbFile)
			if (!file.exists()) {
				Logger.critical(s"SQLite DB(${file.getPath}) file does not exist. Restart Jakon in DEVEL mode to create it.")
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
		dbobj ++= DBHelper.objects
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
					val splitSql = resource.get.split(";")
					splitSql.map(_.trim).filter(_.nonEmpty).foreach(s => {
						var sql = s
						if (Settings.getDatabaseType == DatabaseType.SQLITE) {
							sql = sql.replaceAll("AUTO_INCREMENT", "")
						}
						val stmt = conn.createStatement()
						stmt.execute(sql)
						stmt.close()
					})
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
			checkChildren
			checkColumns
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

	private def checkChildren(implicit conn: Connection): Unit = {
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

	private def checkColumns(implicit conn: Connection): Unit = {
		DBHelper.objects.foreach(jo => {
			checkTableColumns(jo)
			jo.getDeclaredFields.filter(_.getAnnotation(classOf[I18n]) != null).foreach(f => {
				checkI18nTable(jo, f.getAnnotation(classOf[I18n]))
			})
		})
	}

	private def checkTableColumns(jo: Class[?])(implicit conn: Connection): Unit = {
		val tableName = jo.getSimpleName
		val stmt = conn.createStatement()
		val collumns = Settings.getDatabaseType match {
			case DatabaseType.SQLITE =>
				val sql = s"PRAGMA table_info($tableName)"
				val rs = stmt.executeQuery(sql)
				Iterator.from(0).takeWhile(_ => rs.next()).map(_ => TableColumnInfo(rs.getString(2), rs.getString(3))).toSeq
			case DatabaseType.MYSQL =>
				val sql = s"DESCRIBE $tableName"
				val rs = stmt.executeQuery(sql)
				Iterator.from(0).takeWhile(_ => rs.next()).map(_ => TableColumnInfo(rs.getString(1), rs.getString(2))).toSeq
		}
		stmt.close()
		// i18n data lives in a separate table (not a column), which is checked by checkI18nTable
		jo.getDeclaredFields
			.filter(f => f.getAnnotation(classOf[JakonField]) != null && f.getAnnotation(classOf[Transient]) == null && f.getAnnotation(classOf[I18n]) == null)
			.foreach(f => {
				val columnAnn = f.getAnnotation(classOf[Column])
				val manyToOne = f.getAnnotation(classOf[ManyToOne])
				val manyToMany = f.getAnnotation(classOf[ManyToMany])
				val columnName = if (columnAnn != null) {
					columnAnn.name()
				} else if (manyToOne != null) {
					f.getName + "_id"
				} else {
					f.getName
				}
				val column = collumns.find(c => c.name == columnName)
				if (column.isEmpty && manyToMany == null) {
					columnDefinitionFromSqlFile(tableName, columnName) match {
						case Some(columnDefinition) =>
							val alterSql = s"ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition;"
							Logger.error(s"Field ${jo.getSimpleName}.${f.getName} is not in DB, run this SQL to add it: $alterSql")
						case None =>
							Logger.error(s"Field ${jo.getSimpleName}.${f.getName} is not in DB and its definition was not found in sql/$tableName.sql")
					}
				}
				if (manyToMany != null) {
					checkJunctionTable(jo.asInstanceOf[Class[? <: JakonObject]], manyToMany)
				}
			})
	}

	/**
	 * Looks up the column definition (type, NOT NULL, DEFAULT, ...) for the given column from the
	 * table's original CREATE TABLE statement in src/main/resources/sql/$tableName.sql, so that the
	 * suggested ALTER TABLE statement matches the project's own schema definition instead of a type
	 * guessed from the java field.
	 */
	private def columnDefinitionFromSqlFile(tableName: String, columnName: String): Option[String] = {
		Utils.getResourceFromJar(s"/sql/$tableName.sql").flatMap(resource => {
			val openIdx = resource.indexOf('(')
			val closeIdx = resource.lastIndexOf(')')
			if (openIdx < 0 || closeIdx < 0 || closeIdx <= openIdx) {
				None
			} else {
				val body = resource.substring(openIdx + 1, closeIdx)
				splitTopLevelByComma(body)
					.map(_.trim)
					.filter(_.nonEmpty)
					.find(clause => clause.takeWhile(!_.isWhitespace).equalsIgnoreCase(columnName))
					.map(clause => {
						var definition = clause.substring(columnName.length).trim
						if (Settings.getDatabaseType == DatabaseType.SQLITE) {
							definition = definition.replaceAll("AUTO_INCREMENT", "")
						}
						definition
					})
			}
		})
	}

	/** Splits a CREATE TABLE body on commas, ignoring commas nested inside parentheses (e.g. DECIMAL(19, 2)). */
	private def splitTopLevelByComma(s: String): Seq[String] = {
		val result = mutable.ArrayBuffer[String]()
		val current = new StringBuilder
		var depth = 0
		s.foreach {
			case c @ '(' =>
				depth += 1
				current.append(c)
			case c @ ')' =>
				depth -= 1
				current.append(c)
			case ',' if depth == 0 =>
				result += current.toString()
				current.clear()
			case c =>
				current.append(c)
		}
		if (current.nonEmpty) {
			result += current.toString()
		}
		result.toSeq
	}

	private def checkI18nTable(jo: Class[? <: JakonObject], ann: I18n)(implicit conn: Connection): Unit = {
		val i18nClass = ann.genericClass()
		val i18nTable = i18nClass.getSimpleName
		val stmt = conn.createStatement()
		val exists = try {
			stmt.executeQuery(s"SELECT 1 FROM $i18nTable LIMIT 1").close()
			true
		} catch {
			case _: SQLException =>
				Logger.error(s"I18n table $i18nTable for ${jo.getSimpleName} is not in DB")
				false
		} finally {
			stmt.close()
		}
		if (exists) {
			checkTableColumns(i18nClass)
		}
	}

	private def checkJunctionTable(jo: Class[? <: JakonObject], ann: ManyToMany)(implicit conn: Connection): Unit = {
		val targetClass = ann.genericClass()
		val joinTable   = jo.getSimpleName + targetClass.getSimpleName
		val joinCol     = { val n = jo.getSimpleName; n.head.toLower + n.tail + "_id" }
		val inverseCol  = { val n = targetClass.getSimpleName; n.head.toLower + n.tail + "_id" }

		var tableExists = false
		val checkStmt = conn.createStatement()
		try {
			checkStmt.executeQuery(s"SELECT 1 FROM $joinTable LIMIT 1")
			tableExists = true
		} catch {
			case _: SQLException =>
		} finally {
			checkStmt.close()
		}

		if (!tableExists) {
			Logger.info(s"Creating ManyToMany junction table $joinTable")
			val createSql =
				s"""CREATE TABLE IF NOT EXISTS $joinTable (
					 |    $joinCol    INT NOT NULL,
					 |    $inverseCol INT NOT NULL,
					 |    PRIMARY KEY ($joinCol, $inverseCol),
					 |    FOREIGN KEY ($joinCol)    REFERENCES ${jo.getSimpleName} (id) ON DELETE CASCADE,
					 |    FOREIGN KEY ($inverseCol) REFERENCES ${targetClass.getSimpleName} (id) ON DELETE CASCADE
					 |)""".stripMargin
			val createStmt = conn.createStatement()
			createStmt.execute(createSql)
			createStmt.close()
		}
	}

	case class TableColumnInfo(
															name: String,
		                          dataType: String,
														)

}
