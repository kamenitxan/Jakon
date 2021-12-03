package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class DummyValidator extends Validator {
	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		Logger.error("DummyValidator was called")
		Option.empty
	}
}
