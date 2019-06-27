package functions

import cz.kamenitxan.jakon.core.function.FunctionHelper
import test.TestBase

class FunctionHelperTest extends TestBase {
	test("function split params") { _ =>
		val params = FunctionHelper.splitParams("param1=val1 param2=val2")
		assert(params.containsKey("param1"))
		assert(params.get("param2") == "val2")

	}


}