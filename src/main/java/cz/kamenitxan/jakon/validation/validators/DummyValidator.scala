package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class DummyValidator extends Validator {
	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = Option.empty
}
