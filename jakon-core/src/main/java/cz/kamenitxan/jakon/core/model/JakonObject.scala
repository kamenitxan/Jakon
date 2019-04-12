package cz.kamenitxan.jakon.core.model

import java.io.StringWriter
import java.sql._

import cz.kamenitxan.jakon.core.configuration.{DatabaseType, Settings}
import cz.kamenitxan.jakon.core.model.Dao.{Crud, DBHelper}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.json.Json
import javax.persistence._

import scala.annotation.switch
import scala.language.postfixOps

/**
  * Created by TPa on 22.04.16.
  * @param childClass java.lang.Class.getName()
  */
@Inheritance(strategy = InheritanceType.JOINED)
abstract class JakonObject(@JakonField var childClass: String
                          ) extends Serializable with Crud {
	@Id
	@JakonField(disabled = true, required = false, listOrder = -99, searched = true)
	var id: Int = 0
	@JakonField var url: String = ""
	@JakonField var sectionName: String = ""
	@JakonField(listOrder = -95, searched = true)
	var published: Boolean = true

	val objectSettings: ObjectSettings

	def getObjectSettings: ObjectSettings = objectSettings

	def setUrl(url: String): Unit = this.url = url

	def getUrl: String = url


	def executeInsert(stmt: PreparedStatement): Int = {
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

	def create(): Int = {
		val conn = DBHelper.getConnection
		conn.setAutoCommit(false)
		try {
			val joSQL = "INSERT INTO JakonObject (url, sectionName, published, childClass) VALUES (?, ?, ?, ?)"
			val stmt = conn.prepareStatement(joSQL, Statement.RETURN_GENERATED_KEYS)
			stmt.setString(1, url)
			stmt.setString(2, sectionName)
			stmt.setBoolean(3, published)
			stmt.setString(4, childClass)
			val jid = executeInsert(stmt)
			this.id = jid
			val id = createObject(jid, conn)
			if (id != jid) {
				throw new SQLNonTransientException(s"Child object id($id) is not same as parent id($jid)")
			}
			conn.commit()
			jid
		} catch {
			case e: Exception => {
				conn.rollback()
				throw e
			}
		} finally {
			conn.close()
		}
	}

	def createObject(jid: Int, conn: Connection): Int

	def afterCreate(): Unit = {}

	def update(): Unit = {
		val conn = DBHelper.getConnection
		conn.setAutoCommit(false)
		try {
			val joSQL = "UPDATE JakonObject SET url = ?, published = ?, sectionName = ? WHERE id = ?"
			val stmt = conn.prepareStatement(joSQL)
			stmt.setString(1, url)
			stmt.setBoolean(2, published)
			stmt.setString(3, sectionName)
			stmt.setInt(4, id)
			stmt.executeUpdate()
			updateObject(id, conn)
			conn.commit()
		} catch {
			case e: Exception =>
				conn.rollback()
				throw e
		} finally {
			conn.close()
		}
	}

	def updateObject(jid: Int, conn: Connection): Unit

	def afterUpdate(): Unit = {}

	def delete(): Unit = {
		val sql = "DELETE FROM JakonObject WHERE id = ?"
		val stmt = DBHelper.getPreparedStatement(sql)
		stmt.setInt(1, id)
		stmt.executeUpdate()
	}

	def afterDelete(): Unit = {}

	def afterAll(): Unit = {}


	override def toString: String = {
		childClass + "(id: " + id + ")"
	}

	def toJson: String = {
		val writer = new StringWriter
		val generator = Json.createGenerator(writer)
		generator.writeStartObject.write(id).write(url).writeEnd
		generator.close()
		writer.toString
	}
}