package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class NullValidator extends Validator {
	private val error = "NOT_NULL"

	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		if (value == null) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
