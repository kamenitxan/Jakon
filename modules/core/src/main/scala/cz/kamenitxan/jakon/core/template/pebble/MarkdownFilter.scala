package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.core.template.function.FunctionHelper
import io.pebbletemplates.pebble.extension.Filter
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

import java.util
import java.util.regex.Pattern

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

	private val codeBlockPattern = Pattern.compile("(?s)<pre>.*?</pre>")

	def parseString(input: String): String = {
		val document = parser.parse(input)
		val renderedString = renderer.render(document)
		parseFunctionsOutsideCode(renderedString)
	}

	/**
	  * Evaluates template functions like {link ...} everywhere except in code blocks,
	  * where the braces are a part of the shown code.
	  */
	private def parseFunctionsOutsideCode(html: String): String = {
		val m = codeBlockPattern.matcher(html)
		val result = new StringBuilder
		var last = 0
		while (m.find) {
			result.append(FunctionHelper.parse(html.substring(last, m.start())))
			result.append(m.group())
			last = m.end()
		}
		result.append(FunctionHelper.parse(html.substring(last)))
		result.toString
	}

}