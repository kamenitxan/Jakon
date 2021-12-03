package cz.kamenitxan.jakon.webui.entity

import java.sql.{Connection, Statement}
import java.util.Date

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.ManyToOne

class ConfirmEmailEntity extends JakonObject {

	@ManyToOne
	@JakonField(disabled = true)
	var user: JakonUser = _
	@JakonField(disabled = true, shownInList = false)
	var token: String = ""
	@JakonField(disabled = true)
	var secret: String = ""
	@JakonField(disabled = true)
	var expirationDate: Date = _

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

	override def createObject(jid: Int, conn: Connection): Int = {
		val sql = "INSERT INTO ConfirmEmailEntity (id, user_id, token, secret, expirationDate) VALUES (?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setInt(2, user.id)
		stmt.setString(3, token)
		stmt.setString(4, secret)
		stmt.setDate(5, new java.sql.Date(expirationDate.getTime))
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = throw new UnsupportedOperationException
}
