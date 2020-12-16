package core.functions

import java.util

import cz.kamenitxan.jakon.core.function.FunctionHelper
import cz.kamenitxan.jakon.core.model.Page
import org.scalatest.funsuite.AnyFunSuite

/**
  * Created by TPa on 01.09.16.
  */
class LinkTest extends AnyFunSuite{
	test("link test") {
		val page = new Page()
		page.title = "test page"
		page.create()


		val fun = FunctionHelper.getFunction("Link")
		intercept[IllegalArgumentException] {
			fun.execute(null)
		}
		val params = new util.HashMap[String, String]()
		intercept[IllegalArgumentException] {
			fun.execute(params)
		}
		params.put("target", "_blank")
		params.put("id", page.id.toString)
		params.put("text", "linkText")

		assert(fun.execute(params) === "<a href=\"/page/test_page\" target=\"_blank\" >linkText</a>")
	}
}
