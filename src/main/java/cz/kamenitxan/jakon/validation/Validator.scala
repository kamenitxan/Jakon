package cz.kamenitxan.jakon.validation

import java.lang.annotation.Annotation

trait Validator {
	def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult]
}