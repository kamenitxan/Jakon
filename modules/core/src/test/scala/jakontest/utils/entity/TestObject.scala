package jakontest.utils.entity

import cz.kamenitxan.jakon.core.database.annotation.{Embedded, ManyToOne, OneToMany}
import cz.kamenitxan.jakon.core.database.converters.ScalaMapConverter
import cz.kamenitxan.jakon.core.database.{I18n, JakonField}
import cz.kamenitxan.jakon.core.model.{JakonFile, JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.entity.MessageSeverity

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.Date


class TestObject extends JakonObject {
	@ManyToOne
	@JakonField
	var user: JakonUser = scala.compiletime.uninitialized
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
	var time: LocalTime = scala.compiletime.uninitialized
	@JakonField
	var date: Date = scala.compiletime.uninitialized
	@JakonField
	var localDate: LocalDate = scala.compiletime.uninitialized
	@JakonField
	var localDateTime: LocalDateTime = scala.compiletime.uninitialized
	@ManyToOne
	@JakonField
	var self: TestObject = scala.compiletime.uninitialized
	@JakonField
	var javaEnum: MessageSeverity = MessageSeverity.ERROR
	@JakonField(converter = classOf[ScalaMapConverter])
	var map: Map[String, String] = Map()
	@JakonField
	var mapNoConverter: Map[String, String] = Map()
	@OneToMany(genericClass = classOf[JakonUser])
	@JakonField
	var oneToMany: Seq[JakonUser] = scala.compiletime.uninitialized
	@Embedded
	@JakonField
	var embedded: TestEmbeddedObject = scala.compiletime.uninitialized
	@JakonField
	@I18n(classOf[TestObjectI18n])
	var i18n: Seq[TestObjectI18n] = scala.compiletime.uninitialized
	@JakonField
	var jakonFile: JakonFile = scala.compiletime.uninitialized


	override val objectSettings: ObjectSettings = null
}