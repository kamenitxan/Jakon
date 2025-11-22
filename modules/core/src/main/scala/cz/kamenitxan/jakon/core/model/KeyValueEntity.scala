package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement}

/**
  * Created by TPa on 2019-07-03.
  */
class KeyValueEntity extends JakonObject {

	@JakonField
	var name: String = _
	@JakonField
	var value: String = _

	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO KeyValueEntity (id, name, value) VALUES (?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setString(3, value)
		executeInsert(stmt)
	}

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-unlock-alt")

	override def toString = s"KeyValueEntity($name)"

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE KeyValueEntity SET name = ?, value = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setString(2, value)
		stmt.setInt(3, jid)
		stmt.executeUpdate()
	}
}
