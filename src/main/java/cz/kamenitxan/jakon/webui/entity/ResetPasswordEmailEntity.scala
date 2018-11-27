package cz.kamenitxan.jakon.webui.entity

import java.util.Date

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

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")
}
