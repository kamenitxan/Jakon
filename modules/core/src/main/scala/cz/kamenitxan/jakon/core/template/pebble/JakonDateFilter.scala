package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.*
import io.pebbletemplates.pebble.extension.core.DateFilter
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util
import scala.language.postfixOps

class JakonDateFilter extends DateFilter {
	private val formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss")

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		if (input == null) {
			return null
		}
		input.getClass match {
			case DATE_o | SQL_DATE | DATE | DATETIME=> super.apply(input, args, self, context, lineNumber)
			case _ =>
				Logger.warn(s"Unsupported date type: ${input.getClass}. Template: ${self.getName}:$lineNumber")
				input.toString
		}


	}
}
