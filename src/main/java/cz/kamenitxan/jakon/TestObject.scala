package cz.kamenitxan.jakon

import java.time.{LocalDate, LocalDateTime}
import java.util.Date

import cz.kamenitxan.jakon.core.database.{I18n, JakonField}
import cz.kamenitxan.jakon.core.database.converters.ScalaMapConverter
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.MessageSeverity
import javax.persistence.{Embedded, ManyToOne, OneToMany}

class TestObject extends JakonObject {
	@ManyToOne
	@JakonField
	var user: JakonUser = _
	@JakonField
	var string: String = ""
	@JakonField
	var boolean: Boolean = false
	@JakonField
	var double: Double = 1.0
	@JakonField
	var float: Float = 1f
	@JakonField
	var integer: Int = 2
	@JakonField
	var date: Date = _
	@JakonField
	var localDate: LocalDate = _
	@JakonField
	var localDateTime: LocalDateTime = _
	@ManyToOne
	@JakonField
	var self: TestObject = _
	@JakonField
	var enum: MessageSeverity = MessageSeverity.ERROR
	@JakonField(converter = classOf[ScalaMapConverter])
	var map: Map[String, String] = Map()
	@JakonField
	var mapNoConverter: Map[String, String] = Map()
	@OneToMany
	@JakonField
	var oneToMany: Seq[JakonUser] = _

	@JakonField
	@I18n
	var i18n: Seq[TestObjectI18n] = _



	override val objectSettings: ObjectSettings = null
}