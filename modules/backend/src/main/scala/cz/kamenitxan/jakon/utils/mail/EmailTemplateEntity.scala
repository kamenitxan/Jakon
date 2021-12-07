package cz.kamenitxan.jakon.utils.mail

import java.sql.{Connection, Statement}

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.Column

class EmailTemplateEntity extends JakonObject {
	@JakonField(searched = true)
	var name: String = ""
	@Column(name = "addressFrom")
	@JakonField
	var from: String = ""
	@JakonField
	var subject: String = ""
	@JakonField(inputTemplate = "raw_textarea", shownInList = false)
	var template: String = ""

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO EmailTemplateEntity (id, name, addressFrom, subject, template) VALUES (?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setString(3, from)
		stmt.setString(4, subject)
		stmt.setString(5, template)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sql = "UPDATE EmailTemplateEntity SET name = ?, addressFrom = ?, subject = ?, template = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setString(2, from)
		stmt.setString(3, subject)
		stmt.setString(4, template)
		stmt.setInt(5, jid)
		stmt.executeUpdate()
	}
}
