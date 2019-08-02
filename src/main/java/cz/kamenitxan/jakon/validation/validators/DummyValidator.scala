package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class DummyValidator extends Validator {
	override def isValid(value: Any, a: Annotation, data: AnyRef): Option[ValidationResult] = Option.empty
}
