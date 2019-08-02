package cz.kamenitxan.jakon.validation

import java.lang.reflect.Field

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.utils.{PageContext, i18nUtil}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}


object EntityValidator {

	def validate(o: Object): Either[Seq[Message], Object] = {
		val allFields = o.getClass.getDeclaredFields
		val errors = allFields.filter(f => {
			val anns = f.getDeclaredAnnotations
			anns.exists(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		}).flatMap(f => validateField(o, f))

		if (errors.isEmpty) {
			Right(o)
		} else {
			Left(errors)
		}
	}

	private def validateField(o: Object, f: Field): Seq[Message] = {
		if (!f.isAccessible) {
			f.setAccessible(true)
		}
		val fieldValue = f.get(o)
		val anns = f.getDeclaredAnnotations.filter(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		for (an <- anns) {
			val by: ValidatedBy = an.annotationType().getAnnotation(classOf[ValidatedBy])
			val validator: Validator = by.value().newInstance()
			val result = validator.isValid(fieldValue, an, o)
			if (result.isDefined) {
				lazy val severityM = an.annotationType().getDeclaredMethod("severity")
				val severity: MessageSeverity = if (result.get.severity.isDefined) {
					result.get.severity.get
				} else if (severityM != null) {
					severityM.invoke(an).asInstanceOf[MessageSeverity]
				} else {
					MessageSeverity.ERROR
				}
				val key = o.getClass.getSimpleName + "_" + f.getName + "_" + result.get.error
				return Seq(new Message(severity, key, bundle = "validations", params = result.get.params))
			}
		}
		Seq()
	}
}
