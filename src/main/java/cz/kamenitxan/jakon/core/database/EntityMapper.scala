package cz.kamenitxan.jakon.core.database

import java.lang.reflect.Field
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cz.kamenitxan.jakon.core.database.converters.AbstractConverter
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.{BOOLEAN, DATETIME, DATE_o, DOUBLE, FLOAT, INTEGER, STRING}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{Column, Embedded, ManyToOne}

/**
 * Created by TPa on 24.05.2020.
 */
object EntityMapper {

	def createJakonObject[T <: JakonObject](rs: ResultSet, cls: Class[T]): QueryResult[T] = {
		val rsmd = rs.getMetaData
		val obj = cls.getDeclaredConstructor().newInstance()
		var foreignIds: Map[String, ForeignKeyInfo] = Map[String, ForeignKeyInfo]()
		val columnCount = rsmd.getColumnCount


		Iterator.from(1).takeWhile(i => i <= columnCount).foreach(i => {
			var columnName = rsmd.getColumnName(i)
			val fieldName = if (columnName.endsWith("_id")) {
				columnName.substring(0, columnName.length - 3)
			} else {
				columnName
			}


			val fieldRef = Utils.getFieldsUpTo(cls, classOf[Object]).find(f => {
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
				if (columnAnn != null && columnAnn.name() != null && !columnAnn.name().isEmpty) {
					columnName = columnAnn.name()
				}
				val optFKI = setFieldValue(obj, cls, field, columnName, rs)
				if (optFKI.isDefined) {
					foreignIds = foreignIds + optFKI.get
				}
			}
		})
		new QueryResult(obj, foreignIds)
	}

	private def setFieldValue[T <: JakonObject](obj: JakonObject, cls: Class[T],
																							field: Field,
																							columnName: String,
																							rs: ResultSet): Option[(String,  ForeignKeyInfo)] = {
		var foreignIds: (String, ForeignKeyInfo) = null
		field.getType match {
			case STRING => field.set(obj, rs.getString(columnName))
			case BOOLEAN => field.set(obj, rs.getBoolean(columnName))
			case INTEGER => field.set(obj, rs.getInt(columnName))
			case FLOAT => field.set(obj, rs.getFloat(columnName))
			case DOUBLE => field.set(obj, rs.getDouble(columnName))
			case DATE_o => field.set(obj, rs.getDate(columnName))
			case DATETIME => field.set(obj, LocalDateTime.parse(rs.getString(columnName), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
			case x if x.isEnum =>
				val m = x.getMethod("valueOf", classOf[String])
				val enumValue = m.invoke(null, rs.getString(columnName))
				field.set(obj, enumValue)
			case _ =>
				val manyToOne = field.getAnnotation(classOf[ManyToOne])
				lazy val jakonField = field.getAnnotation(classOf[JakonField])
				lazy val embedded = field.getAnnotation(classOf[Embedded])
				if (manyToOne != null) {
					val fv = rs.getInt(columnName)
					if (fv > 0) {
						foreignIds = columnName -> new ForeignKeyInfo(rs.getInt(columnName), columnName, field)
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
