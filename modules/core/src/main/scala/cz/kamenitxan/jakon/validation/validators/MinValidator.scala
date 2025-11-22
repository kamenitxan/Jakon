package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class MinValidator extends Validator {
	private val error = "TOO_SMALL"
	private val nan = "NOT_A_NUMBER"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val ann = a.asInstanceOf[Min]
		val min = ann.value()
		val numberValue = try {
			value.toDouble
		} catch {
			case _: NumberFormatException => return ValidationResult(nan)
		}


		if (numberValue >= min) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
