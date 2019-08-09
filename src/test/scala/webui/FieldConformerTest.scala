package webui

import java.util.{Calendar, Date, GregorianCalendar}

import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.webui.ObjectSettings
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.entity.{JakonField, MessageSeverity}
import javax.persistence.ManyToMany
import org.scalatest.FunSuite

class FieldConformerTest extends FunSuite {

	private val fieldCount = 9

	class TestObject extends JakonObject(classOf[TestObject].getName) {
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
		var date: Date = new Date()
		@JakonField
		var date2: Date = _
		@ManyToMany
		@JakonField
		var self: TestObject = _
		@JakonField
		var enum: MessageSeverity = MessageSeverity.ERROR


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
		val conformed = "1999-02-20".conform(getField("date"))
		val d = new GregorianCalendar(1999, Calendar.FEBRUARY, 20).getTime
		assert(d == conformed)
	}

	test("empty field infos") {
		val fi = FieldConformer.getEmptyFieldInfos(getFields)
		assert(fi.length == fieldCount)
	}

	test("field infos") {
		val fi = FieldConformer.getFieldInfos(new TestObject, getFields)
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
