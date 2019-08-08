package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class AssertFalseValidator extends Validator {
	private val error = "NOT_FALSE"

	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		if (value == null) return Option.empty

		if ("0".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
