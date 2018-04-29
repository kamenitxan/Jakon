package cz.kamenitxan.jakon.webui.conform

import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, HtmlType, JakonField}
import javax.persistence.{ManyToMany, ManyToOne, OneToMany}

import scala.language.postfixOps

object FieldConformer {
	private val S = classOf[String]
	private val B = classOf[Boolean]
	private val D = classOf[java.lang.Double]
	private val I = classOf[java.lang.Integer]
	private val DATE = classOf[Date]
	private val DATETIME = classOf[LocalDateTime]

	//val DATE_FORMAT = "MM/dd/yyyy"
	val DATE_FORMAT = "yyyy-MM-dd"
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
				case _ => {
					if (classOf[JakonObject].isAssignableFrom(c)) {
						val obj = c.newInstance().asInstanceOf[JakonObject]
						obj.id = s.toInt
						obj
					} else {
						s
					}
				}
			}
		}

	}

	def getFieldInfos(obj: JakonObject, fields: List[Field]): List[FieldInfo] = {
		var infos = List[FieldInfo]()
		fields.foreach(f => {
			val an = f.getAnnotation(classOf[JakonField])
			if (an != null) {
				f.setAccessible(true)
				if (f.getAnnotation(classOf[ManyToOne]) != null) {
					val fv = f.get(obj)
					infos = new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "ManyToOne") :: infos
				} else if (f.getAnnotation(classOf[OneToMany]) != null) {
					val fv = f.get(obj)
					infos = new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "OneToMany") :: infos
				} else {
					f.getType match {
						case B =>  {
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.CHECKBOX, f, if (fv != null) fv.toString else null) :: infos
						}
						case DATE => {
							val sdf =  new SimpleDateFormat(DATE_FORMAT)
							if (f.get(obj) != null) {
								val value = sdf.format(f.get(obj))
								infos = new FieldInfo(an, HtmlType.DATE, f, value) :: infos
							} else {
								infos = new FieldInfo(an, HtmlType.DATE, f, value = "") :: infos
							}
						}
						case DATETIME => {
							val sdf = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
							if (f.get(obj) != null) {
								val value = f.get(obj).asInstanceOf[LocalDateTime].format(sdf)
								infos = new FieldInfo(an, HtmlType.DATETIME, f, value) :: infos
							} else {
								infos = new FieldInfo(an, HtmlType.DATETIME, f, value = "") :: infos
							}
						}
						case _ => {
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.TEXT, f, if (fv != null) fv.toString else null) :: infos
						}
					}
				}
				}

		})
		infos.sortBy(fi => fi.an.listOrder)
	}

	def getEmptyFieldInfos(fields: List[Field]): List[FieldInfo] = {
		var infos = List[FieldInfo]()
		fields.foreach(f => {
			val an = f.getAnnotation(classOf[JakonField])
			if (an != null) {
				infos = new FieldInfo(an, f) :: infos
			}
		})
		infos.sortBy(fi => fi.an.listOrder)
	}

}
