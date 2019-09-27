package functions

import java.util

import cz.kamenitxan.jakon.core.function.{FunctionHelper, IFuncion}
import org.scalatest.FunSuite

class FunctionHelperTest extends FunSuite {
	test("function split params") {
		val params = FunctionHelper.splitParams("param1=val1 param2=val2")
		assert(params.containsKey("param1"))
		assert(params.get("param2") == "val2")
	}

	class HelloFun extends IFuncion {
		override def execute(params: util.Map[String, String]): String = "helloWorld"
	}

	test("function parse") {
		FunctionHelper.register(new HelloFun)
		val res = FunctionHelper.parse("{HelloFun()}")
		assert(res == "helloWorld")
	}

}