package webui

import java.util.{Calendar, Date, GregorianCalendar}

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import test.TestBase

class FieldConformerTest extends TestBase {

	var string: String = ""
	var boolean: Boolean = false
	var double: Double = 1.0
	var integer: Integer = 2
	var user: JakonUser = _
	var date: Date = _

	test("conform string") { _ =>
		val conformed = "string".conform(getField("string"))
		assert("string" == conformed)
	}

	test("conform boolean") { _ =>
		val conformed = "true".conform(getField("boolean"))
		assert(true == conformed)
	}

	test("conform double") { _ =>
		val conformed = "1.1".conform(getField("double"))
		assert(1.1 == conformed)
	}

	test("conform integer") { _ =>
		val conformed = "3".conform(getField("integer"))
		assert(3 == conformed)
	}

	test("conform jakonObject") { _ =>
		val conformed = "4".conform(getField("user"))
		val usr = new JakonUser()
		usr.id = 4
		assert(usr.id == conformed.asInstanceOf[JakonUser].id)
	}

	test("conform date") { _ =>
		val conformed = "1999-02-20".conform(getField("date"))
		val d = new GregorianCalendar(1999, Calendar.FEBRUARY, 20).getTime
		assert(d == conformed)
	}


	private def getField(name: String) = {
		val cls = this.getClass
		cls.getDeclaredField(name)
	}
}
