package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class NegativeOrZeroValidator extends Validator {
	private val error = "NOT_NEGATIVE"
	private val nan = "NOT_A_NUMBER"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val numberValue = try {
			value.toDouble
		} catch {
			case _: NumberFormatException => return ValidationResult(nan)
		}

		if (numberValue <= 0) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
