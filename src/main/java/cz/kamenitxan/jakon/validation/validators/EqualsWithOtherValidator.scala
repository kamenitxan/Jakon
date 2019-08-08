package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class EqualsWithOtherValidator extends Validator {
	private val error = "NOT_EQUALS"

	override def isValid(value: String, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		if (value == null) return Option.empty
		val otherFieldName = a.asInstanceOf[EqualsWithOther].value()
		val otherField = data.getClass.getDeclaredField(otherFieldName)
		if (!otherField.isAccessible) {
			otherField.setAccessible(true)
		}
		val otherValue = otherField.get(data).asInstanceOf[String]

		if (value.equals(otherValue)) {
			Option.empty
		} else {
			ValidationResult(error)
		}
	}
}
