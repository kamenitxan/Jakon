package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.text.ParseException
import java.time.format.DateTimeParseException
import java.time.{LocalDate, LocalDateTime}

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}
import cz.kamenitxan.jakon.webui.conform.FieldConformer._

class PastValidator extends Validator {
	private val error = "NOT_PAST"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val res = try {
			val conformedValue = value.conform(field)
			field.getType match {
				case DATE => conformedValue.asInstanceOf[LocalDate].isBefore(LocalDate.now())
				case DATETIME =>
					val now = LocalDateTime.now().withSecond(0).withNano(0)
					conformedValue.asInstanceOf[LocalDateTime].isBefore(now)
				case _ =>
					throw new UnsupportedOperationException
			}
		} catch {
			case ex: DateTimeParseException =>
				Logger.debug("Could not conform date", ex)
				false
			case ex: ParseException =>
				Logger.debug("Could not conform date", ex)
				false
		}

		if (res) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
