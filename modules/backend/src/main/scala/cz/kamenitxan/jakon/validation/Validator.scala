package cz.kamenitxan.jakon.validation

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import scala.language.implicitConversions

trait Validator {
	def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult]

	/** class of field name will not be added to key before translation */
	def fullKeys: Boolean = false

	implicit def result2Opt(validationResult: ValidationResult): Option[ValidationResult] = {
		validationResult.toOpt
	}
}