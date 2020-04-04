package template

import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import org.scalatest.FunSuite

/**
 * Created by TPa on 04.04.2020.
 */
class TemplateUtilsTest extends FunSuite {

	test("parseMarkdown text") {
		val input = "just text"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<p>just text</p>" == res)
	}

	test("parseMarkdown h1") {
		val input = "# title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h1>title</h1>" == res)
	}

	test("parseMarkdown h2") {
		val input = "## title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h2>title</h2>" == res)
	}

	test("parseMarkdown h3") {
		val input = "### title"
		val res = TemplateUtils.parseMarkdown(input)
		assert("<h3>title</h3>" == res)
	}

}
