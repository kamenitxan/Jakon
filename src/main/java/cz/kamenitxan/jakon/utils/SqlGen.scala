package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.sql.{Connection, JDBCType, PreparedStatement, Statement}
import java.time.{LocalDate, LocalTime}
import java.util.Date
import cz.kamenitxan.jakon.core.database.converters.AbstractConverter
import cz.kamenitxan.jakon.core.database.{I18n, JakonField}
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer.TIME_FORMAT

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import javax.persistence.{Embedded, ManyToOne, Transient}
import scala.collection.mutable


object SqlGen {

	private val NumberTypes = classOf[Int] :: classOf[Integer] :: classOf[Double] :: classOf[Float] :: Nil
	private val BoolTypes = classOf[Boolean] :: classOf[java.lang.Boolean] :: Nil

	private def createSql(cls: Class[_ <: JakonObject], annotatedFields: Seq[Field]): String = {
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} (id")
		if (annotatedFields.nonEmpty) {
			sb.append(", ")
			sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
				annotatedFields.head.getType.getGenericSuperclass.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
				annotatedFields.head.getName + "_id"
			} else {
				annotatedFields.head.getName
			})

			var embeddedFieldCounter = 0
			annotatedFields.tail.foreach(f => {
				val fst = f.getType.getGenericSuperclass
				if (fst != null && fst.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
					sb.append(", " + f.getName + "_id")
				} else if (f.getAnnotation(classOf[Embedded]) != null) {
					val embeddedFields = f.getType.getDeclaredFields.filter(_.getDeclaredAnnotation(classOf[JakonField]) != null)
					embeddedFields.foreach(ef => {
						embeddedFieldCounter += 1
						sb.append(", " + f.getName + "_" + ef.getName)
					})
				} else {
					sb.append(", " + f.getName)
				}
			})
			sb.append(") VALUES (?, ?")
			annotatedFields.tail.foreach(_ => sb.append(", ?"))
			if (embeddedFieldCounter > 0) {
				(0 to embeddedFieldCounter).foreach(sb.append(", ?"))
			}
		} else {
			sb.append(") VALUES (?")
		}

		sb.append(");")
		Logger.debug(s"generated sql: ${sb.toString()}")
		sb.toString()
	}

	def insertStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields = getJakonFields(instance.getClass)
		val sql = createSql(instance.getClass, annotatedFields)
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

		stmt.setInt(1, jid)
		filterAndSetValues(instance, annotatedFields, stmt, 2)

		stmt
	}

	private def updateSql(cls: Class[_ <: JakonObject], annotatedFields: Seq[Field]): String = {
		val sb = new StringBuilder
		sb.append(s"UPDATE ${cls.getSimpleName} SET ")

		sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
			annotatedFields.head.getType.getGenericSuperclass.getTypeName == classOf[JakonObject].getName) {
			annotatedFields.head.getName + "_id" + " = ?"
		} else {
			annotatedFields.head.getName + " = ?"
		})

		var embeddedFieldCounter = 0
		annotatedFields.tail.foreach(f => {
			val fst = f.getType.getGenericSuperclass
			if (fst != null && fst.getTypeName == classOf[JakonObject].getName) {
				sb.append(", " + f.getName + "_id = ?")
			} else if (f.getAnnotation(classOf[Embedded]) != null) {
				val embeddedFields = f.getType.getDeclaredFields.filter(_.getDeclaredAnnotation(classOf[JakonField]) != null)
				embeddedFields.foreach(ef => {
					embeddedFieldCounter += 1
					sb.append(", " + f.getName + "_" + ef.getName + " = ?")
				})
			} else {
				sb.append(", " + f.getName + " = ?")
			}
		})
		sb.append(" WHERE id = ?;")
		sb.toString()
	}

	def updateStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields = getJakonFields(instance.getClass)
		val sql = updateSql(instance.getClass, annotatedFields)
		val stmt = conn.prepareStatement(sql)

		filterAndSetValues(instance, annotatedFields, stmt, 1)
		stmt.setInt(annotatedFields.length + 1, jid)

		stmt
	}

	private def filterAndSetValues[T <: JakonObject](instance: T, annotatedFields: Seq[Field],  stmt: PreparedStatement, counterStart: Int): Unit = {
		var counter = counterStart
		for (field <- annotatedFields) {
			if (field.getDeclaredAnnotation(classOf[Embedded]) != null) {
				field.getType
					.getDeclaredFields
					.filter(_.getDeclaredAnnotation(classOf[JakonField]) != null)
					.foreach(f => {
						if (!field.isAccessible) field.setAccessible(true)
						val value = field.get(instance)
						setValue(stmt, f, counter, value)
						counter += 1
					})
			} else {
				setValue(stmt, field, counter, instance)
				counter += 1
			}
		}
	}


	private def getJakonFields(cls: Class[_ <: JakonObject]): Seq[Field] = {
		val allFields = Utils.getFieldsUpTo(cls, cls.getSuperclass)
		allFields.filter(f =>
			f.getAnnotations.exists(fa => fa.annotationType().getName == classOf[JakonField].getName)
				&& f.getName != "id"
				&& f.getAnnotation(classOf[Transient]) == null
			  && f.getAnnotation(classOf[I18n]) == null
		)
	}

	private def setValue(stmt: PreparedStatement, f: Field, i: Int, inst: Any): Unit = {
		if (!f.isAccessible) f.setAccessible(true)

		val value = if (inst != null) {
			f.get(inst)
		} else {
			null
		}
		if (value == null) {
			stmt.setNull(i, getSqlType(f))
			return
		}

		f.getType match {
			case STRING => stmt.setString(i, value.asInstanceOf[String])
			case BOOLEAN => stmt.setBoolean(i, value.asInstanceOf[Boolean])
			case INTEGER => stmt.setInt(i, value.asInstanceOf[Int])
			case FLOAT => stmt.setFloat(i, value.asInstanceOf[Float])
			case DOUBLE => stmt.setDouble(i, value.asInstanceOf[Double])
			case TIME => stmt.setString(i, value.asInstanceOf[LocalTime].format(DateTimeFormatter.ofPattern(FieldConformer.TIME_FORMAT)))
			case DATE => stmt.setDate(i, java.sql.Date.valueOf(value.asInstanceOf[LocalDate]))
			case DATE_o => stmt.setDate(i, new java.sql.Date(value.asInstanceOf[Date].getTime))
			case DATETIME => stmt.setObject(i, value)
			case SEQ => stmt.setString(i, value.asInstanceOf[Seq[JakonObject]].map(_.id).mkString(";"))
			case x if x.isEnum =>
				val nameMethod = value.getClass.getMethod("name")
				stmt.setString(i, nameMethod.invoke(value).toString)
			case _ =>
				lazy val jakonField = f.getAnnotation(classOf[JakonField])
				if (f.getAnnotation(classOf[ManyToOne]) != null) {
					stmt.setInt(i, value.asInstanceOf[JakonObject].id)
				} else if (jakonField != null) {
					val converter = jakonField.converter()
					if (converter.getName != classOf[AbstractConverter[_]].getName) {
						Logger.error(s"Converters are unsupported on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setString(i, "")
					} else {
						Logger.error(s"Convertor not specified for data type on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setNull(i, getSqlType(f))
					}
				} else {
					Logger.error(s"Uknown data type on ${inst.getClass.getSimpleName}.${f.getName}")
					stmt.setNull(i, getSqlType(f))
				}
		}
	}

	def getSqlType(f: Field): Int = {
		f.getType match {
			case x if x.isEnum => JDBCType.VARCHAR.getVendorTypeNumber
			case STRING | TIME => JDBCType.VARCHAR.getVendorTypeNumber
			case BOOLEAN => JDBCType.BOOLEAN.getVendorTypeNumber
			case _ if f.getDeclaredAnnotation(classOf[ManyToOne]) != null => JDBCType.INTEGER.getVendorTypeNumber
			case INTEGER => JDBCType.INTEGER.getVendorTypeNumber
			case FLOAT => JDBCType.FLOAT.getVendorTypeNumber
			case DOUBLE => JDBCType.DOUBLE.getVendorTypeNumber
			case DATE_o | DATE => JDBCType.DATE.getVendorTypeNumber
			case DATETIME => JDBCType.TIMESTAMP.getVendorTypeNumber
			case _ =>
				Logger.error(s"Unknown sql type ${f.getType} on field ${f.getName}")
				0
		}
	}

	def parseFilterParams(kv: mutable.Map[String, String], objectClass: Class[_]): String = {
		if (kv.isEmpty) {
			return ""
		}
		var notFirst = false
		val sb = new mutable.StringBuilder()
		sb.append("WHERE ")
		for ((fieldName, v) <- kv) {
			if (notFirst) {
				sb.append(" AND ")
			}
			val clr = Utils.getClassByFieldName(objectClass, fieldName)


			sb.append(clr._1.getSimpleName)
			sb.append(".")
			sb.append(fieldName)
			if (classOf[JakonObject].isAssignableFrom(clr._2.getType)) {
				sb.append("_id")
			}

			val value = v.trim.toLowerCase
			value match {
				case param if param.contains("*") =>
					sb.append(" LIKE \"")
					sb.append(param.replace("*", "%"))
					sb.append("\"")
				case param =>
					sb.append(" = ")
					if (NumberTypes.contains(clr._2.getType)) {
						try {
							value.toDouble
							sb.append(param)
						} catch {
							case _: NumberFormatException => sb.append("\"" + value + "\"")
						}
					} else if (BoolTypes.contains(clr._2.getType)) {
						try {
							val pbv = value.toBoolean
							if (pbv) sb.append(1) else sb.append(0)
						} catch {
							case _: IllegalArgumentException => sb.append("\"" + value + "\"")
						}
					} else {
						sb.append("\"")
						sb.append(value)
						sb.append("\"")
					}
			}
			notFirst = true
		}
		sb.toString()
	}

}
