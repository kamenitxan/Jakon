package functions

import java.util

import cz.kamenitxan.jakon.core.function.FunctionHelper
import org.scalatest.FunSuite

class HelperTest extends FunSuite {
	test("function helper test") {
		val test = FunctionHelper.parse("{jmenoFunkce param1=val1 param2=val2}")
		val parsed = FunctionHelper.splitParams("param1=val1 param2=val2")

		println(parsed)
		//assert(fun.execute(params) === "<a href=\"/page/testpage1\" target=\"_blank\" >linkText</a>")
	}
}