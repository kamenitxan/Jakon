package cz.kamenitxan.jakon.core.dynamic.validation

import cz.etn.emailvalidator.EmailValidatorBuilder
import cz.kamenitxan.jakon.core.configuration.Settings
import javax.validation.{ConstraintValidator, ConstraintValidatorContext}

class EmailValidatorImpl extends ConstraintValidator[Email, String] {

	private val validator = {
		val builder = new EmailValidatorBuilder()
		builder.setCheckDns(Settings.getEmailValidatorCheckDns)
		builder.build()
	}

	override def initialize(constraintAnnotation: Email): Unit = {
		constraintAnnotation
	}

	override def isValid(value: String, context: ConstraintValidatorContext): Boolean = {
		val email = validator.validate(value)
		email.isValid
		//true
	}
}
