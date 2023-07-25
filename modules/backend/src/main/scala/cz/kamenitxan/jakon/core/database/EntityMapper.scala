package cz.kamenitxan.jakon.core.database

import cz.kamenitxan.jakon.core.database.annotation.{Column, Embedded, ManyToOne, OneToMany}
import cz.kamenitxan.jakon.core.database.converters.AbstractConverter
import cz.kamenitxan.jakon.core.model.{BaseEntity, JakonObject}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.conform.FieldConformer

import java.lang.reflect.Field
import java.sql.ResultSet
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime}

/**
 * Created by TPa on 24.05.2020.
 */
object EntityMapper {

	def createJakonObject[T <: BaseEntity](rs: ResultSet, cls: Class[T]): QueryResult[T] = {
		val rsmd = rs.getMetaData
		val obj = cls.getDeclaredConstructor().newInstance()
		var foreignIds: Map[String, ForeignKeyInfo] = Map[String, ForeignKeyInfo]()
		val columnCount = rsmd.getColumnCount
		val fields = Utils.getFieldsUpTo(cls, classOf[Object])


		Iterator.from(1).takeWhile(i => i <= columnCount).foreach(i => {
			var columnName = rsmd.getColumnName(i)
			val fieldName = if (columnName.endsWith("_id")) {
				columnName.substring(0, columnName.length - 3)
			} else {
				columnName
			}


			val fieldRef = fields.find(f => {
				val byName = f.getName.equalsIgnoreCase(fieldName)
				lazy val byAnn = {
					val ann = f.getDeclaredAnnotation(classOf[Column])
					if (ann != null) {
						fieldName == ann.name()
					} else {
						false
					}
				}
				byName || byAnn
			})

			if (fieldRef.nonEmpty) {
				val field = fieldRef.get
				field.setAccessible(true)
				val columnAnn = field.getAnnotation(classOf[Column])
				if (columnAnn != null && columnAnn.name() != null && columnAnn.name().nonEmpty) {
					columnName = columnAnn.name()
				}
				val optFKI = setFieldValue(obj, cls, field, columnName, rs)
				if (optFKI.isDefined) {
					foreignIds = foreignIds + optFKI.get
				}
			}
		})

		val i18nField = Utils.getFieldsUpTo(cls, classOf[Object]).find(_.getDeclaredAnnotation(classOf[I18n]) != null)
		new QueryResult(obj, foreignIds, i18nField)
	}

	private def setFieldValue[T <: BaseEntity](obj: BaseEntity, cls: Class[T],
																						 field: Field,
																						 columnName: String,
																						 rs: ResultSet): Option[(String, ForeignKeyInfo)] = {
		var foreignIds: (String, ForeignKeyInfo) = null
		field.getType match {
			case STRING => field.set(obj, rs.getString(columnName))
			case BOOLEAN => field.set(obj, rs.getBoolean(columnName))
			case INTEGER => field.set(obj, rs.getInt(columnName))
			case INTEGER_j => field.set(obj, rs.getInt(columnName))
			case FLOAT => field.set(obj, rs.getFloat(columnName))
			case DOUBLE => field.set(obj, rs.getDouble(columnName))
			case DATE_o => field.set(obj, rs.getDate(columnName))
			case DATE => field.set(obj, Option(rs.getDate(columnName)).map(_.toLocalDate).orNull)
			case TIME =>
				val formatter = DateTimeFormatter.ofPattern(FieldConformer.TIME_FORMAT)
				val v = Option(rs.getString(columnName)).map(dt => LocalTime.parse(dt, formatter)).orNull
				field.set(obj, v)
			case DATETIME =>
				val v = Option(rs.getString(columnName)).map(dt => LocalDateTime.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)).orNull
				field.set(obj, v)
			case x if x.isEnum =>
				val m = x.getMethod("valueOf", classOf[String])
				val value = rs.getString(columnName)
				val enumValue = if (value != null) m.invoke(null, value) else null
				field.set(obj, enumValue)
			case _ =>
				val manyToOne = field.getAnnotation(classOf[ManyToOne])
				lazy val oneToMany = field.getAnnotation(classOf[OneToMany])
				lazy val jakonField = field.getAnnotation(classOf[JakonField])
				lazy val embedded = field.getAnnotation(classOf[Embedded])
				if (manyToOne != null) {
					val fv = rs.getInt(columnName)
					if (fv > 0) {
						foreignIds = columnName -> new ForeignKeyInfo(Seq(rs.getInt(columnName)), columnName, field)
					}
				} else if (oneToMany != null) {
					val fv = rs.getString(columnName)
					if (fv != null && fv.nonEmpty) {
						foreignIds = columnName -> new ForeignKeyInfo(fv.split(";").toSeq.map(id => id.toInt), columnName, field)
					} else {
						field.set(obj, Seq.empty)
					}
				} else if (jakonField != null) {
					val converter = jakonField.converter()
					if (converter.getName != classOf[AbstractConverter[_]].getName) {
						field.set(obj, converter.getDeclaredConstructor().newInstance().convertToEntityAttribute(rs.getString(columnName)))
					} else {
						Logger.error(s"Converter not specified for data type on ${obj.getClass.getSimpleName}.${field.getName}")
					}
				} else if (embedded != null) {
					val embeddedObj = field.getType.getDeclaredConstructor().newInstance()

					val fields = field.getType.getDeclaredFields.filter(_.getDeclaredAnnotation(classOf[JakonField]) != null).toSeq
					fields.foreach(f => {
						val fieldType = f.getType.asSubclass(classOf[JakonObject])
						setFieldValue(embeddedObj.asInstanceOf[JakonObject], fieldType, f, field.getName + "_" + getColumnName(f), rs)
					})

					field.set(obj, embeddedObj)
				} else {
					Logger.warn("Unknown data type on " + cls.getSimpleName + s".${field.getName}")
				}
		}
		Option.apply(foreignIds)
	}

	private def getColumnName(field: Field): String = {
		val columnAnn = field.getDeclaredAnnotation(classOf[Column])
		if (columnAnn != null && columnAnn.name().nonEmpty) {
			columnAnn.name()
		} else {
			val fkAnn = field.getDeclaredAnnotation(classOf[ManyToOne])
			val fieldName = field.getName
			if (fkAnn != null) {
				fieldName + "_id"
			} else {
				fieldName
			}
		}
	}

}
