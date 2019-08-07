package cz.kamenitxan.jakon.validation.validators


import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class NotEmptyValidator extends Validator {
	private val error = "EMPTY"

	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		val ann = a.asInstanceOf[NotEmpty]
		if (value == null) {
			return ValidationResult(error).toOpt
		}
		if (value.nonEmpty) {
			Option.empty
		} else {
			ValidationResult(error).toOpt
		}
	}
}
