package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.core.template.function.FunctionHelper
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
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

	private val extensions = util.List.of(TablesExtension.create, StrikethroughExtension.create)
	private val parser = Parser.builder.extensions(extensions).build
	private val renderer = HtmlRenderer.builder
		.extensions(extensions)
		.softbreak("<br>")
		.build

	def parseString(input: String): String = {
		val document = parser.parse(input)
		val renderedString = renderer.render(document)
		val result = FunctionHelper.parse(renderedString)
		result
	}

}