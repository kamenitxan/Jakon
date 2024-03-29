package jakontest.core.functions

import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.function.FunctionHelper
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.util

/**
  * Created by TPa on 01.09.16.
  */
@DoNotDiscover
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
