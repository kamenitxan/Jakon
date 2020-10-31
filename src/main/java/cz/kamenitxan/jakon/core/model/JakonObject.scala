package cz.kamenitxan.jakon.core.model

import java.io.StringWriter
import java.sql._

import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.database.{Crud, DBHelper}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.{SqlGen, Utils}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.json.Json
import javax.persistence._

import scala.annotation.switch
import scala.language.postfixOps

/**
  * Created by TPa on 22.04.16.
  */
abstract class JakonObject(implicit s: sourcecode.FullName) extends Serializable with Crud {

	@Id
	@JakonField(disabled = true, required = false, listOrder = -99, searched = true)
	var id: Int = 0
	@JakonField
	var url: String = ""
	@JakonField(listOrder = -95, searched = true)
	var published: Boolean = true
	@JakonField
	var childClass: String = s.value

	val objectSettings: ObjectSettings

	def getObjectSettings: ObjectSettings = objectSettings

	def createUrl: String = url


	final def executeInsert(stmt: PreparedStatement): Int = {
		val affectedRows = stmt.executeUpdate
		if (affectedRows == 0) {
			throw new SQLException("Creating JakonObject failed, no rows affected.")
		}

		val generatedKeys = stmt.getGeneratedKeys
		if (generatedKeys.next()) {
			generatedKeys.getInt(1)
		} else {
			(Settings.getDatabaseType: @switch) match {
				case DatabaseType.SQLITE => throw new SQLException("Creating JakonObject failed, no id obtained.")
				case DatabaseType.MYSQL => {
					if (this.getClass == classOf[JakonObject]) {
						throw new SQLException("Creating JakonObject failed, no id obtained.")
					} else {
						this.id
					}
				}
			}
		}
	}

	final def create(): Int = {
		val conn = DBHelper.getConnection
		conn.setAutoCommit(false)
		try {
			val joSQL = "INSERT INTO JakonObject (url, published, childClass) VALUES (?, ?, ?)"
			val stmt = conn.prepareStatement(joSQL, Statement.RETURN_GENERATED_KEYS)
			url = createUrl
			stmt.setString(1, url)
			stmt.setBoolean(2, published)
			stmt.setString(3, childClass)
			val jid = executeInsert(stmt)
			this.id = jid
			val id = createObject(jid, conn)
			if (id != jid) {
				throw new SQLNonTransientException(s"Child object id($id) is not same as parent id($jid)")
			}
			afterCreate()
			afterAll()
			conn.commit()
		} catch {
			case e: Exception => {
				conn.rollback()
				throw e
			}
		} finally {
			conn.close()
		}
		if (this.getClass.getInterfaces.contains(classOf[Ordered])) {
			val  thisOrdered = DBHelper.withDbConnection(conn2 => this.asInstanceOf[JakonObject with Ordered].updateNewObjectOrder(conn2))
			thisOrdered.update()
		}
		this.id
	}

	def createObject(jid: Int, conn: Connection): Int = {
		Logger.warn(s"createObject method is not overridden for $childClass")
		val stmt = SqlGen.insertStmt(this, conn, jid)
		executeInsert(stmt)
	}

	def afterCreate(): Unit = {
		// this will be executed after JakonObject creation
	}

	final def update(): Unit = {
		val conn = DBHelper.getConnection
		conn.setAutoCommit(false)
		try {
			val joSQL = "UPDATE JakonObject SET url = ?, published = ? WHERE id = ?"
			val stmt = conn.prepareStatement(joSQL)
			stmt.setString(1, createUrl)
			stmt.setBoolean(2, published)
			stmt.setInt(3, id)
			stmt.executeUpdate()
			updateObject(id, conn)
			afterUpdate()
			afterAll()
			conn.commit()
		} catch {
			case e: Exception =>
				conn.rollback()
				throw e
		} finally {
			conn.close()
		}
	}

	def updateObject(jid: Int, conn: Connection): Unit = {
		if (Utils.getFields(this.getClass).nonEmpty) {
			Logger.warn(s"updateObject method is not overridden $childClass")
			val stmt = SqlGen.updateStmt(this, conn, jid)
			stmt.executeUpdate()
		}
	}

	def afterUpdate(): Unit = {
		// this will be executed after JakonObject update
	}

	def delete(): Unit = {
		val sql = "DELETE FROM JakonObject WHERE id = ?"
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, id)
			stmt.executeUpdate()
		})
		afterDelete()
		afterAll()
	}

	def afterDelete(): Unit = {
		// this will be executed after JakonObject deletion
	}

	def afterAll(): Unit = {
		// this will be executed after JakonObject creation, update or deletion
	}


	override def toString: String = {
		childClass + s"(id: $id)"
	}

	def toJson: String = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		writer.toString
	}
}