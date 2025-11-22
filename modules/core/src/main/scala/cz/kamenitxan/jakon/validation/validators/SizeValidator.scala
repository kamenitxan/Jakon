package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class SizeValidator extends Validator {
	private val small = "TOO_SMALL"
	private val large = "TOO_LARGE"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		val ann = a.asInstanceOf[Size]
		if (value == null) {
			return Option.empty
		}
		val length = value.length()
		if (length < ann.min()) {
			ValidationResult(small)
		} else if (length > ann.max()) {
			ValidationResult(large)
		} else {
			Option.empty
		}
	}
}
