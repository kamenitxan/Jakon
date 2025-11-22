package cz.kamenitxan.jakon.validation.validators


import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class NotEmptyValidator extends Validator {
	private val error = "EMPTY"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) {
			return ValidationResult(error)
		}
		if (value.nonEmpty) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
