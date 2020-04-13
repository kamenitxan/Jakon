package cz.kamenitxan.jakon.webui.conform

import java.lang.reflect.{Field, ParameterizedType, Type}
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, HtmlType, JakonField}
import javax.persistence.{ManyToOne, OneToMany}

import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object FieldConformer {

	val DATE_FORMAT = "yyyy-MM-dd"
	val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm"

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
				case BOOLEAN => s toBoolean
				case DOUBLE | DOUBLE_j => s toDouble
				case INTEGER | INTEGER_j => s toInt
				case DATE =>
					val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
					LocalDate.parse(s, formatter)
				case DATE_o =>
					val sdf = new SimpleDateFormat(DATE_FORMAT)
					sdf.parse(s)
				case DATETIME =>
					val formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
					LocalDateTime.parse(s, formatter)
				case LIST_j | ARRAY_LIST_j =>
					s.split("\r\n").map(line => line.trim.conform(Class.forName(genericType.getTypeName), null)).toList.asJava
				case SEQ =>
					s.split("\r\n").map(line => line.trim.conform(Class.forName(genericType.getTypeName), null)).toSeq
				case x if x.isEnum =>
					val m = x.getMethod("valueOf", classOf[String])
					m.invoke(t, s)
				case _ =>
					if (classOf[JakonObject].isAssignableFrom(t)) {
						val obj = t.getDeclaredConstructor().newInstance().asInstanceOf[JakonObject]
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
				if (f.getDeclaredAnnotation(classOf[ManyToOne]) != null) {
					val fv = f.get(obj)
					infos = new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "ManyToOne") :: infos
				} else if (f.getDeclaredAnnotation(classOf[OneToMany]) != null) {
					val fv = f.get(obj)
					infos = new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "OneToMany") :: infos
				} else {
					f.getType match {
						case BOOLEAN =>
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.CHECKBOX, f, if (fv != null) fv.toString else null) :: infos
						case INTEGER | INTEGER_j =>
							val fv = f.get(obj)
							infos = new FieldInfo(an, HtmlType.NUMBER, f, fv) :: infos
						case DATE_o =>
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
