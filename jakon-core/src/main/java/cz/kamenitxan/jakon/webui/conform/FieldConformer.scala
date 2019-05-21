package cz.kamenitxan.jakon.webui.conform

import java.lang.reflect.{Field, ParameterizedType, Type}
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, HtmlType, JakonField}
import javax.persistence.{ManyToOne, OneToMany}

import scala.collection.JavaConverters._
import scala.language.postfixOps

object FieldConformer {
	private val S = classOf[String]
	private val B = classOf[Boolean]
	private val D = classOf[java.lang.Double]
	private val D_s = classOf[Double]
	private val I = classOf[java.lang.Integer]
	private val I_s = classOf[Int]
	private val LIST = classOf[java.util.List[Any]]
	private val DATE = classOf[Date]
	private val DATETIME = classOf[LocalDateTime]

	//val DATE_FORMAT = "MM/dd/yyyy"
	val DATE_FORMAT = "yyyy-MM-dd"
	val DATETIME_FORMAT = "MM/dd/yyyy'T'HH:mm"

	implicit class StringConformer(val s: String) {


		def conform(f: Field): Any = {
			if (s == null || s.isEmpty) {
				return null
			}
			var gft: Array[Type] = null
			try {
				gft = f.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments
			} catch {
				case _: Exception =>
			}
			conform(f.getType, if (gft != null) gft.head else null)
		}

		private def conform(t: Class[_], genericType: Type): Any = {
			t match {
				case B => s toBoolean
				case D | D_s => s toDouble
				case I | I_s => s toInt
				case DATE =>
					val sdf = new SimpleDateFormat(DATE_FORMAT)
					sdf.parse(s)
				case DATETIME =>
					val sdf = new SimpleDateFormat()
					sdf.parse(s)
				case LIST =>
					s.split("\r\n").map(line => line.conform(Class.forName(genericType.getTypeName), null)).toList.asJava
				case x if x.isEnum =>
					val m = x.getMethod("valueOf", classOf[String])
					m.invoke(t, s)
				case _ =>
					if (classOf[JakonObject].isAssignableFrom(t)) {
						val obj = t.newInstance().asInstanceOf[JakonObject]
						obj.id = s.toInt
						obj
					} else {
						s
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
						case B =>
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.CHECKBOX, f, if (fv != null) fv.toString else null) :: infos
						case I | I_s =>
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.NUMBER, f, fv) :: infos
						case DATE =>
							val sdf = new SimpleDateFormat(DATE_FORMAT)
							if (f.get(obj) != null) {
								val value = sdf.format(f.get(obj))
								infos = new FieldInfo(an, HtmlType.DATE, f, value) :: infos
							} else {
								infos = new FieldInfo(an, HtmlType.DATE, f, value = "") :: infos
							}
						case DATETIME =>
							val sdf = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
							if (f.get(obj) != null) {
								val value = f.get(obj).asInstanceOf[LocalDateTime].format(sdf)
								infos = new FieldInfo(an, HtmlType.DATETIME, f, value) :: infos
							} else {
								infos = new FieldInfo(an, HtmlType.DATETIME, f, value = "") :: infos
							}
						case x if x.isEnum =>
							val fv = f.get(obj)
							val fi = new FieldInfo(an, HtmlType.SELECT, f, fv, "enum")
							fi.extraData.put("enumValues", x.getEnumConstants)
							infos = fi :: infos
						case _ =>
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.TEXT, f, fv) :: infos
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
