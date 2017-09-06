package cz.kamenitxan.jakon.webui.conform

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, JakonField}

object FieldConformer {
	val S = classOf[String]
	val B = classOf[Boolean]
	val D = classOf[java.lang.Double]
	val DATE = classOf[Date]

	implicit class StringConformer(val s: String) {


		def conform(c: Class[_]): Any = {
			if (s == null || s.isEmpty) {
				return null
			}
			c match {
				case B => s toBoolean
				case D => s toDouble
				case DATE => {
					val sdf = new SimpleDateFormat("MM/dd/yyyy")
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
						infos = new FieldInfo(an.required(), an.disabled(), "checkbox", an.htmlClass(), if (fv != null) fv.toString else null, f.getName, templateName) :: infos
					}
					case DATE => {
						val sdf = new SimpleDateFormat("MM/dd/yyyy")
						if (f.get(obj) != null) {
							val value = sdf.format(f.get(obj))
							infos = new FieldInfo(an.required(), an.disabled(), "date", an.htmlClass(), value, f.getName, templateName) :: infos
						} else {
							infos = new FieldInfo(an.required(), an.disabled(), "date", an.htmlClass(), value = "", f.getName, templateName) :: infos
						}
					}
					case _ => {
						val fv = f.get(obj)
						infos = new FieldInfo(an.required(), an.disabled(), "text", an.htmlClass(), if (fv != null) fv.toString else null, f.getName, templateName) :: infos
					}
				}
			}
		})
		infos
	}

}
