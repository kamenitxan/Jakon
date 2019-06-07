package utils

import cz.kamenitxan.jakon.Main
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}
import cz.kamenitxan.jakon.utils.{TypeReferences, Utils}
import cz.kamenitxan.jakon.utils.Utils._
import org.scalatest.FunSuite


class UtilsTest extends FunSuite {

	test("getClassByFieldName") {
		val cls = Utils.getClassByFieldName(classOf[JakonUser], "id")
		assert(classOf[JakonObject] == cls._1)
		assert("id" == cls._2.getName)
	}

	test("StringImprovements") {
		val s = "".getOrElse("ok")
		assert("ok" == s)

		val s2 = "test".getOrElse("ok")
		assert("test" == s2)
	}

	test("Main") {
		val app = new Main.JakonApp
		assert(app != null)
	}

	test("TypeReferences") {
		assert(TypeReferences.STRING != null)
	}

}
