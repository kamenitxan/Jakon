package cz.kamenitxan.jakon.validation

import cz.kamenitxan.jakon.core.dynamic.arguments.ParsedValue
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import java.lang.annotation.Annotation
import java.lang.reflect.{Constructor, Field}
import scala.io.Source


object EntityValidator {

	def validate(prefix: String, validatedData: Map[Field, String]): Either[Seq[Message], Map[Field, String]] = {
		val errors = validatedData.filter(f => {
			val anns = f._1.getDeclaredAnnotations
			anns.exists(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		}).flatMap(f => {
			val anns = f._1.getDeclaredAnnotations.filter(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null).toSeq
			validateField(prefix, f._1, anns, f._2, validatedData)
		}).toSeq

		if (errors.isEmpty) {
			Right(validatedData)
		} else {
			Left(errors)
		}
	}
	
	// TODO: change name?
	def validateJson(prefix: String, validatedData: Map[Field, ParsedValue]): Either[Seq[Message], Map[Field, ParsedValue]] = {
		val errors = validatedData.map(data => {
			val constructor: Constructor[_] = data._1.getDeclaringClass.getDeclaredConstructors.head
			val parameterIndex = constructor.getParameters.zipWithIndex.find(pi => {
				pi._1.getName == data._1.getName
			}).map(_._2)
			if (parameterIndex.isEmpty) {
				Logger.critical("Failed to find constructor param")
				(data._1, data._2, Array.empty[Annotation])
			} else {
				val anns: Array[Annotation] = constructor.getParameterAnnotations()(parameterIndex.get)
				(data._1, data._2, anns)
			}

		}).filter(data => {
			data._3.exists(a => a.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
		}).flatMap(data => {
			// TODO: nested objects validation
			val oldFormatData = validatedData.map(data => (data._1, data._2.stringValue))
			val anns = data._3.filter(_.annotationType().getAnnotation(classOf[ValidatedBy]) != null)
			validateField(prefix, data._1, anns.toSeq, data._2.stringValue, oldFormatData)
		}).toSeq


		if (errors.isEmpty) {
			Right(validatedData)
		} else {
			Left(errors)
		}
	}

	// TODO: tohle tu byt nema, neni to cast validatoru
	def createFormData(ctx: Context, dataClass: Class[_]): Map[Field, String] = {
		dataClass.getDeclaredFields.map(f => {
			var res: (Field, String) = null
			try {
				f.setAccessible(true)

				val value = if (ctx.isMultipartFormData) {
					val is = ctx.req().getPart(f.getName).getInputStream
					Source.fromInputStream(is).mkString
				} else {
					val paramName = Option.apply(f.getDeclaredAnnotation(classOf[Name])).map(_.name()).getOrElse(f.getName)
					ctx.queryParam(paramName)
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

	private def validateField(prefix: String,
														f: Field,
														validators: Seq[Annotation],
														fieldValue: String,
														validatedData: Map[Field, String]): Seq[Message] = {
		if (!f.isAccessible) {
			f.setAccessible(true)
		}
		for (an <- validators) {
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
