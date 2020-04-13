package template

import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import org.scalatest.funsuite.AnyFunSuite

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

}
