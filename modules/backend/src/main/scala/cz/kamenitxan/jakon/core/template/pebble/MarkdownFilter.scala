package cz.kamenitxan.jakon.core.template.pebble

import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.template.function.FunctionHelper
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.util

class MarkdownFilter extends Filter {

	override def getArgumentNames: util.List[String] = null

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		MarkdownFilter.parseString(input.asInstanceOf[String])
	}
}

object MarkdownFilter {

	def parseString(input: String): String = {
		val parser = Parser.builder.build
		val document = parser.parse(input)
		val renderer = HtmlRenderer.builder.build
		val renderedString = renderer.render(document)
		  .replace("\r\n", "<br>")
		  .replace("\n", "<br>")
		  .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
		val result = FunctionHelper.parse(renderedString)
		result
	}

}