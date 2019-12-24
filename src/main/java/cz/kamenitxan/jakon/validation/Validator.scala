package cz.kamenitxan.jakon.validation

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import scala.language.implicitConversions

trait Validator {
	def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult]

	implicit def result2Opt(validationResult: ValidationResult): Option[ValidationResult] = {
		validationResult.toOpt
	}
}