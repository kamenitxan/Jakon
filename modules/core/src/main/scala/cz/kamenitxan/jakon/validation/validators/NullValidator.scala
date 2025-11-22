package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class NullValidator extends Validator {
	private val error = "NOT_NULL"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
