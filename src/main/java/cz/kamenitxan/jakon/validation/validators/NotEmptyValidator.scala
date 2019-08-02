package cz.kamenitxan.jakon.validation.validators


import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}

class NotEmptyValidator extends Validator {
	private val error = "EMPTY"

	override def isValid(value: Any, a: Annotation, data: AnyRef): Option[ValidationResult] = {
		val ann = a.asInstanceOf[NotEmpty]
		if (value == null) {
			return ValidationResult(error).toOpt
		}
		val nonEmpty = value match {
			case traversable: Traversable[Any] =>
				traversable.nonEmpty
			case string: CharSequence =>
				string.length() > 0
			case _ =>
				true
		}

		if (nonEmpty) {
			Option.empty
		} else {
			ValidationResult(error).toOpt
		}
	}
}
