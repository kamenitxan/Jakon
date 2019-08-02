package cz.kamenitxan.jakon.validation

import cz.kamenitxan.jakon.webui.entity.MessageSeverity

case class ValidationResult(error: String, severity: Option[MessageSeverity] = Option.apply(MessageSeverity.ERROR), params: Seq[String] = Seq()) {


	def toOpt: Option[ValidationResult] = {
		Option.apply(this)
	}
}
object ValidationResult {
	def of(error: String, severity: MessageSeverity, params: Seq[String]) = ValidationResult(error, Option.apply(severity), params)
	def of(error: String, severity: MessageSeverity): ValidationResult = this(error, Option.apply(severity))
}