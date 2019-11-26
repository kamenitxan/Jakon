package cz.kamenitxan.jakon.core.template.pebble

import java.util

import com.github.rjeschke.txtmark.Processor
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.function.FunctionHelper

class MarkdownFilter extends Filter {

	override def getArgumentNames: util.List[String] = null

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		MarkdownFilter.parseString(input.asInstanceOf[String])
	}
}

object MarkdownFilter {

	def parseString(input: String): String = {
		val renderedMarkdown = Processor.process(input)
		val result = FunctionHelper.parse(renderedMarkdown)
		result
	}

}