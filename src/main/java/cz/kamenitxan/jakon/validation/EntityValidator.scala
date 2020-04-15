package cz.kamenitxan.jakon.validation

import java.lang.reflect.Field

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.Request


object EntityValidator {

	def validate(prefix: String, validatedData: Map[Field, String]): Either[Seq[Message], Map[Field, String]] = {
		val errors = validatedData.filter(f => {
			val anns = f._1.getDeclaredAnnotations
			anns.exists(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		}).flatMap(f => validateField(prefix, f._1, f._2, validatedData)).toSeq

		if (errors.isEmpty) {
			Right(validatedData)
		} else {
			Left(errors)
		}
	}

	def createFormData(req: Request, dataClass: Class[_]): Map[Field, String] = {
		dataClass.getDeclaredFields.map(f => {
			var res: (Field, String) = null
			try {
				f.setAccessible(true)
				res = (f, req.queryParams(f.getName))
			} catch {
				case ex: Exception => Logger.error("Exception when setting pagelet form data value", ex)
			}
			res
		}).filter(t => t != null).toMap
	}

	def createFormData(data: Any): Map[Field, String] = {
		data.getClass.getDeclaredFields.map(f => {
			var res: (Field, String) = null
			try {
				f.setAccessible(true)
				res = (f, f.get(data).toString)
			} catch {
				case ex: Exception => Logger.error("Exception when setting pagelet form data value", ex)
			}
			res
		}).filter(t => t != null).toMap
	}

	private def validateField(prefix: String, f: Field, fieldValue: String, validatedData: Map[Field, String]): Seq[Message] = {
		if (!f.isAccessible) {
			f.setAccessible(true)
		}
		val anns = f.getDeclaredAnnotations.filter(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		for (an <- anns) {
			val by: ValidatedBy = an.annotationType().getAnnotation(classOf[ValidatedBy])
			val validator: Validator = by.value().newInstance()
			val result = validator.isValid(fieldValue, an, f, validatedData)
			if (result.isDefined) {
				lazy val severityM = an.annotationType().getDeclaredMethod("severity")
				val severity: MessageSeverity = if (result.get.severity.isDefined) {
					result.get.severity.get
				} else if (severityM != null) {
					severityM.invoke(an).asInstanceOf[MessageSeverity]
				} else {
					MessageSeverity.ERROR
				}
				val key = prefix + "_" + f.getName + "_" + result.get.error
				return Seq(new Message(severity, key, bundle = "validations", params = result.get.params))
			}
		}
		Seq()
	}
}
