package cz.kamenitxan.jakon.utils.mail

import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{Column, Entity}

import scala.beans.BeanProperty

@Entity
class EmailEntity extends JakonObject(classOf[EmailEntity].getName) {
	@BeanProperty @Column @JakonField
	var from: String = ""
	@BeanProperty @Column @JakonField
	var subject: String = ""
	@BeanProperty @Column @JakonField
	var sent: Boolean = false
	@BeanProperty @Column @JakonField
	var sentDate: Date = _
	@BeanProperty @Column @JakonField
	var template: String = ""
	@BeanProperty @Column @JakonField
	var emailType: String = _
	@BeanProperty @Column @JakonField
	var params: Map[String, Any] = _

	override val objectSettings: ObjectSettings = null
}
