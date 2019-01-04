package cz.kamenitxan.jakon.utils.mail

import java.sql.{Connection, Statement, Types}

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{Column, Entity}

import scala.beans.BeanProperty

@Entity
class EmailTemplateEntity(u: Unit = ()) extends JakonObject(classOf[EmailTemplateEntity].getName) {
	@BeanProperty @Column @JakonField(searched = true)
	var name: String = ""
	@BeanProperty @Column(name = "addressFrom") @JakonField
	var from: String = ""
	@BeanProperty @Column @JakonField
	var subject: String = ""
	@BeanProperty @Column @JakonField(inputTemplate = "textarea")
	var template: String = ""

	def this() = this(u=())

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

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}
