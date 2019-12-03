package cz.kamenitxan.jakon.validation.validators

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import cz.etn.emailvalidator.EmailValidatorBuilder
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.validation.{ValidationResult, Validator}
import cz.kamenitxan.jakon.webui.entity.MessageSeverity

class EmailValidator extends Validator {

	override def isValid(value: String, a: Annotation, field: Field, data: Map[Field, String]): Option[ValidationResult] = {
		val ann = a.asInstanceOf[Email]
		if (value == null) {
			return Option.empty
		}
		val validatedEmail = EmailValidator.validator.validate(value.asInstanceOf[String])
		if (validatedEmail.isValid) {
			if(!validatedEmail.email.getWarnings.isEmpty) {
				val suggestion = validatedEmail.email.getSuggestion
				Option.apply(ValidationResult.of(validatedEmail.email.getWarnings.get(0).name(), MessageSeverity.WARNING, Seq(suggestion)))
			} else {
				Option.empty
			}
		} else {
			if (validatedEmail.email.getError != null) {
				Option.apply(ValidationResult.of(validatedEmail.email.getError.name(), MessageSeverity.ERROR))
			} else {
				Option.apply(ValidationResult.of(validatedEmail.email.getWarnings.get(0).name(), MessageSeverity.ERROR))
			}
		}
	}
}

object EmailValidator {
	private val validator = {
		val builder = new EmailValidatorBuilder()
		builder.setCheckDns(Settings.getEmailValidatorCheckDns)
		builder.build()
	}
}
