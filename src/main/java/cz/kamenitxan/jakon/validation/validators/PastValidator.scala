package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.time.LocalDate

import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}
import cz.kamenitxan.jakon.webui.conform.FieldConformer._

class PastValidator extends Validator {
	private val error = "NOT_PAST"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val conformedValue = value.conform(field)
		val res = field.getType match {
			case DATE_o => throw new UnsupportedOperationException
			case DATE => conformedValue.asInstanceOf[LocalDate].isBefore(LocalDate.now())
		}

		if (res) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
