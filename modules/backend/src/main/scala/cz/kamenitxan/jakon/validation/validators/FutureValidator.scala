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

class FutureValidator extends Validator {
	private val error = "NOT_PAST"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val res = try {
			val conformedValue = value.conform(field)
			field.getType match {
				case DATE => conformedValue.asInstanceOf[LocalDate].isAfter(LocalDate.now())
				case DATETIME =>
					val now = LocalDateTime.now().withSecond(0).withNano(0)
					conformedValue.asInstanceOf[LocalDateTime].isAfter(now)
				case _ =>
					throw new UnsupportedOperationException
			}
		} catch {
			case _: DateTimeParseException | _: ParseException =>
				Logger.debug(s"Could not conform date. Cannot parse '$value'")
				false
			case _: UnsupportedOperationException =>
				Logger.debug("Could not conform date. Field type is not LocalDate or LocalDateTime")
				false
		}

		if (res) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
