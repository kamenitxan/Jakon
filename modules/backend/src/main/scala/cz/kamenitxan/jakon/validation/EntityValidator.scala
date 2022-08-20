package cz.kamenitxan.jakon.validation

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.Request

import java.lang.reflect.Field
import scala.io.Source


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

	// TODO: tohle tu byt nema, neni to cast validatoru
	def createFormData(req: Request, dataClass: Class[_]): Map[Field, String] = {
		dataClass.getDeclaredFields.map(f => {
			var res: (Field, String) = null
			try {
				f.setAccessible(true)

				val value = if (req.raw().getContentType.startsWith("multipart/form-data")) {
					val is = req.raw().getPart(f.getName).getInputStream
					Source.fromInputStream(is).mkString
				} else {
					val paramName = Option.apply(f.getDeclaredAnnotation(classOf[Name])).map(_.name()).getOrElse(f.getName)
					req.queryParams(paramName)
				}

				res = (f, value)
			} catch {
				case ex: Exception => Logger.error("Exception when setting pagelet form data value", ex)
			}
			res
		}).filter(t => t != null).toMap
	}

	// TODO: tohle tu byt nema, neni to cast validatoru
	def createFormData(data: Any): Map[Field, String] = {
		data.getClass.getDeclaredFields.map(f => {
			var res: (Field, String) = null
			try {
				f.setAccessible(true)
				val fieldValue = f.get(data)
				res = (f, if fieldValue == null then "" else fieldValue.toString)
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
			val validator: Validator = by.value().getDeclaredConstructor().newInstance()
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
				val key = if (!validator.fullKeys) prefix + "_" + f.getName + "_" + result.get.error else result.get.error
				return Seq(new Message(severity, key, bundle = "validations", params = result.get.params))
			}
		}
		Seq()
	}
}
