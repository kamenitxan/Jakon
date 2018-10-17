package cz.kamenitxan.jakon.utils.mail

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.mail.Message
import javax.persistence.{Column, Entity}

import scala.beans.BeanProperty

@Entity
class EmailTemplateEntity extends JakonObject(classOf[EmailTemplateEntity].getName) {
	@BeanProperty @Column @JakonField(searched = true)
	var name: String = ""
	@BeanProperty @Column @JakonField
	var to: String = ""
	@BeanProperty @Column @JakonField
	var subject: String = ""
	@BeanProperty @Column @JakonField
	var template: String = ""


	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")
}
