package cz.kamenitxan.jakon.webui.conform

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, JakonField}

object FieldConformer {
	val S = classOf[String]
	val B = classOf[Boolean]
	val D = classOf[java.lang.Double]
	val I = classOf[java.lang.Integer]
	val DATE = classOf[Date]
	val DATETIME = classOf[LocalDateTime]

	val DATE_FORMAT = "MM/dd/yyyy"
	val DATETIME_FORMAT = "MM/dd/yyyy'T'HH:mm"

	implicit class StringConformer(val s: String) {


		def conform(c: Class[_]): Any = {
			if (s == null || s.isEmpty) {
				return null
			}
			c match {
				case B => s toBoolean
				case D => s toDouble
				case I => s toInt
				case DATE => {
					val sdf = new SimpleDateFormat(DATE_FORMAT)
					sdf.parse(s)
				}
				case DATETIME => {
					val sdf = new SimpleDateFormat()
					sdf.parse(s)
				}
				case _ => s
			}
		}

	}

	def getFieldInfos(obj: JakonObject, fields: List[Field]): List[FieldInfo] = {
		var infos = List[FieldInfo]()
		fields.foreach(f => {
			val an = f.getAnnotation(classOf[JakonField])
			if (an != null) {
				f.setAccessible(true)
				val templateName = if (an.inputTemplate().isEmpty) {f.getType.getSimpleName} else {an.inputTemplate()}
				f.getType match {
					case B =>  {
						val fv = f.get(obj)
						infos = new FieldInfo(an.required(), an.disabled(), "checkbox", an.htmlClass(), an.htmlMaxLength(), if (fv != null) fv.toString else null, f.getName, templateName) :: infos
					}
					case DATE => {
						val sdf =  new SimpleDateFormat(DATE_FORMAT)
						if (f.get(obj) != null) {
							val value = sdf.format(f.get(obj))
							infos = new FieldInfo(an.required(), an.disabled(), "date", an.htmlClass(), an.htmlMaxLength(), value, f.getName, templateName) :: infos
						} else {
							infos = new FieldInfo(an.required(), an.disabled(), "date", an.htmlClass(), an.htmlMaxLength(), value = "", f.getName, templateName) :: infos
						}
					}
					case DATETIME => {
						val sdf = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
						if (f.get(obj) != null) {
							val value = f.get(obj).asInstanceOf[LocalDateTime].format(sdf)
							infos = new FieldInfo(an.required(), an.disabled(), "datetime-local", an.htmlClass(), an.htmlMaxLength(), value, f.getName, templateName) :: infos
						} else {
							infos = new FieldInfo(an.required(), an.disabled(), "datetime-local", an.htmlClass(), an.htmlMaxLength(), value = "", f.getName, templateName) :: infos
						}
					}
					case _ => {
						val fv = f.get(obj)
						infos = new FieldInfo(an.required(), an.disabled(), "text", an.htmlClass(), an.htmlMaxLength(), if (fv != null) fv.toString else null, f.getName, templateName) :: infos
					}
				}
			}
		})
		infos
	}

}
