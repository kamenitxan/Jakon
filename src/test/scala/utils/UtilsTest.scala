package utils

import cz.kamenitxan.jakon.Main
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.utils.{TypeReferences, Utils}
import org.scalatest.funsuite.AnyFunSuite


class UtilsTest extends AnyFunSuite {

	test("getClassByFieldName") {
		val cls = Utils.getClassByFieldName(classOf[JakonUser], "id")
		assert(classOf[JakonObject] == cls._1)
		assert("id" == cls._2.getName)
	}

	test("StringImprovements getOrElse") {
		val s = "".getOrElse("ok")
		assert("ok" == s)

		val s2 = "test".getOrElse("ok")
		assert("test" == s2)
	}

	test("StringImprovements isNullOrEmpty") {
		assert("".isNullOrEmpty)
		val nil: String = null
		assert(nil.isNullOrEmpty)
		assert(!"test".isNullOrEmpty)
	}

	test("StringImprovements toBoolOrFalse") {
		assert("true".toBoolOrFalse)
		val nil: String = null
		assert(!nil.toBoolOrFalse)
		assert(!"false".isNullOrEmpty)
		assert(!"invalid".isNullOrEmpty)
	}

	test("StringImprovements urlEncode") {
		assert("AAAAEOadfo0q8yu1kwuF%2FACMMcOszbXH%2BDTWsP5Da3MijNXA" == "AAAAEOadfo0q8yu1kwuF/ACMMcOszbXH+DTWsP5Da3MijNXA".urlEncode)
		val nil: String = null
		assert(nil.urlEncode == null)
	}

	test("Main") {
		val app = new Main.JakonApp
		assert(app != null)
	}

	test("TypeReferences") {
		assert(TypeReferences.STRING != null)
	}



}
