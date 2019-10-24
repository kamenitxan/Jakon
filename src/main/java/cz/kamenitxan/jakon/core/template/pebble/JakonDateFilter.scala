package cz.kamenitxan.jakon.core.template.pebble

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util

import com.mitchellbosecke.pebble.extension.core.DateFilter
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

import scala.language.postfixOps

class JakonDateFilter extends DateFilter {
	val formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss")

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		if (!input.isInstanceOf[LocalDateTime]) {
			return super.apply(input, args, self, context, lineNumber)
		}
		val date = input.asInstanceOf[LocalDateTime]
		date.format(formatter)
	}
}
