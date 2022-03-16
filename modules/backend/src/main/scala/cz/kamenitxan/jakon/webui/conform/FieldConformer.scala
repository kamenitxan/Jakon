package cz.kamenitxan.jakon.webui.conform

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.annotation.{ManyToOne, OneToMany}
import cz.kamenitxan.jakon.core.database.{I18n, JakonField}
import cz.kamenitxan.jakon.core.model.{I18nData, JakonObject}
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.controller.impl.ObjectController
import cz.kamenitxan.jakon.webui.entity.{FieldInfo, HtmlType}

import java.lang.reflect.{Field, ParameterizedType, Type}
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object FieldConformer {

	val TIME_FORMAT = "HH:mm"
	val DATE_FORMAT = "yyyy-MM-dd"
	val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm"
	val i18nExcludedFields = ObjectController.excludedFields ++ Seq("locale", "id", "className")

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
				case FLOAT => s toFloat
				case INTEGER | INTEGER_j => s toInt
				case DATE =>
					val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
					LocalDate.parse(s, formatter)
				case DATE_o =>
					val sdf = new SimpleDateFormat(DATETIME_FORMAT)
					sdf.parse(s)
				case TIME =>
					val formatter = DateTimeFormatter.ISO_TIME
					LocalTime.parse(s, formatter)
				case DATETIME =>
					val formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
					LocalDateTime.parse(s, formatter)
				case LIST_j | ARRAY_LIST_j =>
					s.split("\r\n").map(line => line.trim.conform(Class.forName(genericType.getTypeName), null)).toList.asJava
				case SEQ =>
					s.split("\r\n").map(line => line.trim.conform(Class.forName(genericType.getTypeName), null)).toSeq
				case LOCALE => Utils.stringToLocale(s)
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


	def getFieldInfos(obj: Any, fields: Seq[Field]): Seq[FieldInfo] = {
		var infos = Seq[FieldInfo]()
		fields.foreach(f => {
			val an = f.getAnnotation(classOf[JakonField])
			lazy val i18nAn = f.getAnnotation(classOf[I18n])
			lazy val i18nTypeCls = f.getGenericType.asInstanceOf[ParameterizedType].getActualTypeArguments.head
			if (an != null) {
				f.setAccessible(true)
				if (f.getDeclaredAnnotation(classOf[ManyToOne]) != null) {
					val fv = f.get(obj)
					infos = infos :+ new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "ManyToOne")
				} else if (f.getDeclaredAnnotation(classOf[OneToMany]) != null) {
					val fv = f.get(obj)
					val typeCls = f.getCollectionGenericType
					val typeName = typeCls.getTypeName.substring(typeCls.getTypeName.lastIndexOf(".") + 1)
					infos = infos :+ new FieldInfo(an, HtmlType.CHECKBOX, f, fv, "OneToMany", typeName)
				} else {
					f.getType match {
						case BOOLEAN =>
							val fv = f.get(obj)
							infos = infos :+ new FieldInfo(an, HtmlType.CHECKBOX, f, if (fv != null) fv.toString else null)
						case INTEGER | INTEGER_j =>
							val fv = f.get(obj)
							infos = infos :+ new FieldInfo(an, HtmlType.NUMBER, f, fv)
						case DATE_o =>
							val sdf = new SimpleDateFormat(DATETIME_FORMAT)
							if (f.get(obj) != null) {
								val value = sdf.format(f.get(obj))
								infos = infos :+ new FieldInfo(an, HtmlType.DATETIME, f, value)
							} else {
								infos = infos :+ new FieldInfo(an, HtmlType.DATETIME, f, value = "")
							}
						case TIME =>
							val sdf = DateTimeFormatter.ofPattern(TIME_FORMAT)
							if (f.get(obj) != null) {
								val value = sdf.format(f.get(obj).asInstanceOf[LocalTime])
								infos = infos :+ new FieldInfo(an, HtmlType.TIME, f, value)
							} else {
								infos = infos :+ new FieldInfo(an, HtmlType.TIME, f, value = "")
							}
						case DATE =>
							val sdf = DateTimeFormatter.ofPattern(DATE_FORMAT)
							if (f.get(obj) != null) {
								val value = f.get(obj).asInstanceOf[LocalDate].format(sdf)
								infos = infos :+ new FieldInfo(an, HtmlType.DATE, f, value)
							} else {
								infos = infos :+ new FieldInfo(an, HtmlType.DATE, f, value = "")
							}
						case DATETIME =>
							val sdf = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
							if (f.get(obj) != null) {
								val value = f.get(obj).asInstanceOf[LocalDateTime].format(sdf)
								infos = infos :+ new FieldInfo(an, HtmlType.DATETIME, f, value)
							} else {
								infos = infos :+ new FieldInfo(an, HtmlType.DATETIME, f, value = "")
							}
						case LOCALE =>
							val fv = f.get(obj)
							val fi = new FieldInfo(an, HtmlType.SELECT, f, fv, template = "locale_select")
							fi.extraData.put("supportedLocales", Settings.getSupportedLocales.asJava)
							infos = infos :+ fi
						case x if x.isEnum =>
							val fv = f.get(obj)
							val fi = new FieldInfo(an, HtmlType.SELECT, f, fv, "enum")
							fi.extraData.put("enumValues", x.getEnumConstants)
							infos = infos :+ fi
						case _ if i18nAn != null =>
							val fv: Seq[I18nData] = if (f.get(obj) == null) {
								Seq.empty
							} else {
								f.get(obj).asInstanceOf[Seq[I18nData]]
							}
							val fi = new FieldInfo(an, HtmlType.TEXT, f, fv, "i18n")
							fi.extraData.put("locales", Settings.getSupportedLocales)

							val fields = Utils.getFieldsUpTo(f.getCollectionGenericTypeClass, classOf[Object]).filter(n => !i18nExcludedFields.contains(n.getName))
							fi.extraData.put("fieldNames", fields.map(_.getName))


							val fuu: Map[String, FieldInfo] = Settings.getSupportedLocales.flatMap(l => {
								if (fv.nonEmpty) {

									fields.map(f => {
										val key = f.getName + "_" + l.toString
										val value = fv.find(_.locale == l)
																	.map(
																		d => FieldConformer.getFieldInfos(d, fields).find(_.name == f.getName).get
																	)
										key -> value.get
									})
								} else {
									fields.map(f => f.getName + "_" + l.toString -> {
										val an = f.getAnnotation(classOf[JakonField])
										new FieldInfo(an, HtmlType.TEXT, f, null, "String")
									})
								}
							}).toMap

							fi.extraData.put("i18nFields", fuu)
							infos = infos :+ fi
						case _ =>
							val fv = f.get(obj)
							infos = infos :+ new FieldInfo(an, HtmlType.TEXT, f, fv)
					}
				}
			}

		})
		infos.sortBy(fi => fi.an.listOrder)
	}

	def getEmptyFieldInfos(fields: Seq[Field]): Seq[FieldInfo] = {
		var infos = Seq[FieldInfo]()
		fields.foreach(f => {
			val an = f.getAnnotation(classOf[JakonField])
			if (an != null) {
				infos = infos :+ new FieldInfo(an, f)
			}
		})
		infos.sortBy(fi => fi.an.listOrder)
	}

}
