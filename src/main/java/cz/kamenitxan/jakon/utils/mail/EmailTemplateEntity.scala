package cz.kamenitxan.jakon.utils.mail

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
}
