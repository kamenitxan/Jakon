package cz.kamenitxan.jakon.webui.entity

import java.sql.Connection
import java.util.Date

import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import javax.persistence.{Column, Entity}

import scala.beans.BeanProperty

@Entity
class ConfirmEmailEntity(u: Unit = ()) extends JakonObject(classOf[ConfirmEmailEntity].getName) {

	@BeanProperty @Column @JakonField(disabled = true)
	var user: JakonUser = _
	@BeanProperty
	@Column
	@JakonField(disabled = true, shownInList = false)
	var token: String = ""
	@BeanProperty @Column @JakonField(disabled = true)
	var secret: String = ""
	@BeanProperty @Column @JakonField(disabled = true)
	var expirationDate: Date = _

	def this() = this(u=())

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

	override def createObject(jid: Int, conn: Connection): Int = ???

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}
