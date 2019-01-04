package cz.kamenitxan.jakon.utils.mail

import java.sql.Connection
import java.util.Date

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.core.model.converters.ScalaMapConverter
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{Column, Convert, Entity}

import scala.beans.BeanProperty

@Entity
class EmailEntity(u: Unit = ()) extends JakonObject(classOf[EmailEntity].getName) {
	@BeanProperty @Column(name = "addressTo") @JakonField
	var to: String = ""
	@BeanProperty @Column @JakonField
	var subject: String = ""
	@BeanProperty @Column @JakonField
	var sent: Boolean = false
	@BeanProperty @Column @JakonField(disabled = true)
	var sentDate: Date = _
	@BeanProperty @Column @JakonField
	var template: String = _
	@BeanProperty @Column @JakonField
	var lang: String = _
	@BeanProperty @Column @JakonField
	var emailType: String = _
	@BeanProperty
	@Column
	@JakonField(shownInList = false)
	@Convert(converter = classOf[ScalaMapConverter])
	var params: Map[String, AnyRef] = _

	def this() = this(u=())

	def this(template: String, to: String, subject: String, params: Map[String, AnyRef]) = {
		this(u=())
		this.template = template
		this.to = to
		this.subject = subject
		this.lang = Settings.getDefaultLocale.getCountry
		this.params = params
	}

	def this(template: String, to: String, subject: String, params: Map[String, AnyRef], emailType: String) = {
		this(u=())
		this.template = template
		this.to = to
		this.subject = subject
		this.params = params
		this.emailType = emailType
		this.lang = Settings.getDefaultLocale.getCountry
	}

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

	override def createObject(jid: Int, conn: Connection): Int = ???

	override def updateObject(jid: Int, conn: Connection): Unit = ???
}
