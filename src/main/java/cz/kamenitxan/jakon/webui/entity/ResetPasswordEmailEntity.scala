package cz.kamenitxan.jakon.webui.entity

import java.sql.Statement
import java.util.Date

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.{Column, Entity}

import scala.beans.BeanProperty

@Entity
class ResetPasswordEmailEntity(u: Unit = ()) extends JakonObject(classOf[ResetPasswordEmailEntity].getName) {

	@BeanProperty
	@Column
	@JakonField(disabled = true)
	var user: JakonUser = _
	@BeanProperty
	@Column
	@JakonField(disabled = true, shownInList = false)
	var token: String = ""
	@BeanProperty
	@Column
	@JakonField(disabled = true)
	var secret: String = ""
	@BeanProperty
	@Column
	@JakonField(disabled = true)
	var expirationDate: Date = _

	def this() = this(u = ())


	override def create(): Int = {
		val jid = super.create()
		val sql = "INSERT INTO ResetPasswordEmailEntity (id, user, token, secret, expirationDate) VALUES (?, ?, ?, ?, ?)"
		val stmt = DBHelper.getPreparedStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setInt(2, user.id)
		stmt.setString(3, token)
		stmt.setString(4, secret)
		stmt.setDate(5, new java.sql.Date(expirationDate.getTime))
		executeInsert(stmt)
	}

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")
}
