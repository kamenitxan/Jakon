package cz.kamenitxan.jakon.validation

import java.lang.annotation.Annotation

import scala.language.implicitConversions

trait Validator {
	def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult]

	implicit def result2Opt(validationResult: ValidationResult): Option[ValidationResult] = {
		validationResult.toOpt
	}
}