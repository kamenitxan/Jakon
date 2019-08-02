package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class SizeValidator extends Validator {
	private val small = "TOO_SMALL"
	private val large = "TOO_LARGE"

	override def isValid(value: Any, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		val ann = a.asInstanceOf[Size]
		if (value == null) {
			return Option.empty
		}
		val lenght = value match {
			case traversable: Traversable[Any] =>
				traversable.size
			case string: CharSequence =>
				string.length()
			case _ =>
				0
		}

		if (lenght < ann.min()) {
			ValidationResult(small).toOpt
		} else if (lenght > ann.max()) {
			ValidationResult(large).toOpt
		} else {
			Option.empty
		}
	}
}
