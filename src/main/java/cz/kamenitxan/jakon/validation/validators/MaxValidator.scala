package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class MaxValidator extends Validator {
	private val error = "TOO_BIG"
	private val nan = "NOT_A_NUMBER"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		val ann = a.asInstanceOf[Max]
		val max = ann.value()
		val numberValue = try {
			value.toDouble
		} catch {
			case _: NumberFormatException => return ValidationResult(nan)
		}


		if (numberValue <= max) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
