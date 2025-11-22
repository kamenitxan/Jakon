package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class AssertTrueValidator extends Validator {
	private val error = "NOT_TRUE"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty

		if ("1".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
