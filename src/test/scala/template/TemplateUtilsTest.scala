package template

import cz.kamenitxan.jakon.core.function.FunctionHelper
import cz.kamenitxan.jakon.core.model.Post
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import org.scalatest.funsuite.AnyFunSuite
import test.HelloFun

/**
 * Created by TPa on 04.04.2020.
 */
class TemplateUtilsTest extends AnyFunSuite {

	test("parseMarkdown text") {
		val input = "just text"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<p>just text</p>\n" == res)
	}

	test("parseMarkdown h1") {
		val input = "# title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h1>title</h1>\n" == res)
	}

	test("parseMarkdown h2") {
		val input = "## title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h2>title</h2>\n" == res)
	}

	test("parseMarkdown h3") {
		val input = "### title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h3>title</h3>\n" == res)
	}

	test("post null content") {
		val p = new Post
		p.content = null
		assert(p.getContent == "")
	}

	test("post no content") {
		val p = new Post
		assert(p.getContent == "")
	}

	test("post markdown content") {
		val p = new Post
		p.content =
			"""# title
				|just text
				|""".stripMargin
		assert(p.getContent == "<h1>title</h1>\n<p>just text</p>\n")
	}

	test("post fun content") {
		FunctionHelper.register(new HelloFun)

		val p = new Post
		p.content = "{HelloFun()}"
		assert(p.getContent == "<p>helloWorld</p>")
	}

}
