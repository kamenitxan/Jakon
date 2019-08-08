package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class SizeValidator extends Validator {
	private val small = "TOO_SMALL"
	private val large = "TOO_LARGE"

	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		val ann = a.asInstanceOf[Size]
		if (value == null) {
			return Option.empty
		}
		val lenght = value.length()
		if (lenght < ann.min()) {
			ValidationResult(small)
		} else if (lenght > ann.max()) {
			ValidationResult(large)
		} else {
			Option.empty
		}
	}
}
