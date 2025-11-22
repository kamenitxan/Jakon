package cz.kamenitxan.jakon.validation.validators

import cz.etn.emailvalidator.EmailValidatorBuilder
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.validation.validators.EmailValidator.prefix
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}
import cz.kamenitxan.jakon.webui.entity.MessageSeverity

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class EmailValidator extends Validator {
	override def fullKeys: Boolean = true

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		if (value == null) {
			return Option.empty
		}
		val validatedEmail = EmailValidator.validator.validate(value)
		if (validatedEmail.isValid) {
			val ann = a.asInstanceOf[Email]
			if(!validatedEmail.email.getWarnings.isEmpty && ann.suggestions()) {
				val suggestion = validatedEmail.email.getSuggestion
				Option.apply(ValidationResult.of(prefix + validatedEmail.email.getWarnings.get(0).name(), MessageSeverity.WARNING, Seq(suggestion)))
			} else {
				Option.empty
			}
		} else {
			if (validatedEmail.email.getError != null) {
				Option.apply(ValidationResult.of(prefix + validatedEmail.email.getError.name(), MessageSeverity.ERROR))
			} else {
				Option.apply(ValidationResult.of(prefix + validatedEmail.email.getWarnings.get(0).name(), MessageSeverity.ERROR))
			}
		}
	}
}

object EmailValidator {
	private val prefix = "EmailValidator_"

	private val validator = {
		val builder = new EmailValidatorBuilder()
		builder.setCheckDns(Settings.getEmailValidatorCheckDns)
		builder.build()
	}
}
