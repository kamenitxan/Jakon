package webui

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToMany
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.conform.FieldConformer.*
import cz.kamenitxan.jakon.webui.conform.{FieldConformer, GenericType}
import cz.kamenitxan.jakon.webui.entity.MessageSeverity
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util
import java.util.{Calendar, Date, GregorianCalendar}

@DoNotDiscover
class FieldConformerTest extends AnyFunSuite {

	private val fieldCount = 17

	class TestObject extends JakonObject {
		//TODO oneToMany

		@JakonField
		var string: String = ""
		@JakonField
		var boolean: Boolean = false
		@JakonField
		var double: Double = 1.0
		@JakonField
		var integer: Integer = 2
		@JakonField
		var user: JakonUser = _
		@JakonField
		var time: LocalTime = _
		@JakonField
		var date: Date = new Date()
		@JakonField
		var date2: Date = _
		@JakonField
		var localDate: LocalDate = _
		@JakonField
		var localDateTime: LocalDateTime = _
		@ManyToMany
		@JakonField
		var self: TestObject = _
		@JakonField
		var javaEnum: MessageSeverity = MessageSeverity.ERROR
		@JakonField
		@GenericType(cls = classOf[Integer])
		var listJInt: util.ArrayList[Integer] = _
		@JakonField
		@GenericType(classOf[String])
		var listJString: util.ArrayList[String] = _
		@JakonField
		var seqI: Seq[Int] = _
		@JakonField
		@GenericType(classOf[String])
		var seqS: Seq[String] = _
		@JakonField
		var float: Float = _


		override val objectSettings: ObjectSettings = null
	}

	test("conform null") {
		val nill: String = null
		val conformed = nill.conform(getField("string"))
		assert(null == conformed)
	}

	test("conform string") {
		val conformed = "string".conform(getField("string"))
		assert("string" == conformed)
	}

	test("conform boolean") {
		val conformed = "true".conform(getField("boolean"))
		assert(true == conformed)
	}

	test("conform double") {
		val conformed = "1.1".conform(getField("double"))
		assert(1.1 == conformed)
	}

	test("conform integer") {
		val conformed = "3".conform(getField("integer"))
		assert(3 == conformed)
	}

	test("conform jakonObject") {
		val conformed = "4".conform(getField("user"))
		val usr = new JakonUser()
		usr.id = 4
		assert(usr.id == conformed.asInstanceOf[JakonUser].id)
	}

	test("conform date") {
		val conformed = "1999-02-20T20:20".conform(getField("date"))
		val d = new GregorianCalendar(1999, Calendar.FEBRUARY, 20, 20, 20).getTime
		assert(d == conformed)
	}

	test("conform localDate") {
		val conformed = "1999-02-20".conform(getField("localDate"))
		val d = LocalDate.of(1999, 2, 20)
		assert(d == conformed)
	}

	test("conform list_j integer") {
		val conformed = "1\r\n2\r\n3".conform(getField("listJInt"))
		assert(conformed.asInstanceOf[util.List[Integer]].contains(1))
		assert(conformed.asInstanceOf[util.List[Integer]].contains(2))
		assert(conformed.asInstanceOf[util.List[Integer]].contains(3))
	}

	test("conform list_j string") {
		val conformed = "1\r\n2\r\n3".conform(getField("listJString"))
		assert(conformed.asInstanceOf[util.List[String]].contains("1"))
		assert(conformed.asInstanceOf[util.List[String]].contains("2"))
		assert(conformed.asInstanceOf[util.List[String]].contains("3"))
	}

	test("conform list_j empty") {
		val conformed = "".conform(getField("listJString"))
		assert(conformed == null)
	}

	// TODO
	/*test("conform seq integer") {
		val conformed = "1\r\n2\r\n3".conform(getField("seqI"))
		assert(conformed.asInstanceOf[Seq[Integer]].contains(1))
		assert(conformed.asInstanceOf[Seq[Integer]].contains(2))
		assert(conformed.asInstanceOf[Seq[Integer]].contains(3))
	}*/

	test("conform seq string") {
		val conformed = "1\r\n2\r\n3".conform(getField("seqS"))
		assert(conformed.asInstanceOf[Seq[String]].contains("1"))
		assert(conformed.asInstanceOf[Seq[String]].contains("2"))
		assert(conformed.asInstanceOf[Seq[String]].contains("3"))
	}

	test("conform float") {
		val conformed = "1.1".conform(getField("float"))
		assert(1.1f == conformed)
	}

	test("conform time") {
		val conformed = "10:19".conform(getField("time"))
		val t = LocalTime.of(10, 19)
		assert(t == conformed)
	}

	test("conform javaEnum") {
		val conformed = "ERROR".conform(getField("javaEnum"))
		val t = MessageSeverity.ERROR
		assert(t == conformed)
	}

	test("empty field infos") {
		val fi = FieldConformer.getEmptyFieldInfos(getFields)
		assert(fi.length == fieldCount)
	}

	test("field infos") {
		val fi = FieldConformer.getFieldInfos(new TestObject, getFields)
		assert(fi.length == fieldCount)
	}

	test("field infos with data") {
		val o = new TestObject
		o.time = LocalTime.now()
		o.localDate = LocalDate.now()
		o.localDateTime = LocalDateTime.now()
		val fi = FieldConformer.getFieldInfos(o, getFields)
		assert(fi.length == fieldCount)
	}

	private def getField(name: String) = {
		val cls = classOf[TestObject]
		cls.getDeclaredField(name)
	}

	private def getFields = {
		val cls = classOf[TestObject]
		cls.getDeclaredFields.toList
	}
}
