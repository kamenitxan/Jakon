package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.utils.TypeReferences.*
import com.mitchellbosecke.pebble.extension.core.DateFilter
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

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
			case DATE_o | SQL_DATE => super.apply(input, args, self, context, lineNumber)
			case DATE =>
				val date = input.asInstanceOf[LocalDate]
				date.format(DateTimeFormatter.ISO_DATE)
			case DATETIME =>
				val date = input.asInstanceOf[LocalDateTime]
				date.format(formatter)
		}


	}
}
