package cz.kamenitxan.jakon.validation.validators

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class EqualsWithOtherValidator extends Validator {
	private val error = "NOT_EQUALS"

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) return Option.empty
		val otherFieldName = a.asInstanceOf[EqualsWithOther].value()
		val otherValue = data.find(kv => kv._1.getName == otherFieldName).map(kv => kv._2).orNull
		if (value.equals(otherValue)) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
