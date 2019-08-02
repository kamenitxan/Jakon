package cz.kamenitxan.jakon.validation

import java.lang.annotation.Annotation

trait Validator {
	def isValid(value: Any, a: Annotation, data: AnyRef): Option[ValidationResult]
}