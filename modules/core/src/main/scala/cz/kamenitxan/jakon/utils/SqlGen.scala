package cz.kamenitxan.jakon.utils

import com.google.gson.reflect.TypeToken
import cz.kamenitxan.jakon.core.database.annotation.{Embedded, ManyToOne, Transient}
import cz.kamenitxan.jakon.core.database.converters.{AbstractConverter, NoOpConverter}
import cz.kamenitxan.jakon.core.database.{I18n, JakonField}
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.webui.conform.FieldConformer

import java.lang.reflect.Field
import java.sql.{Connection, JDBCType, PreparedStatement, Statement}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}
import java.util.Date
import scala.collection.mutable


object SqlGen {

	private val NumberTypes = classOf[Int] :: classOf[Integer] :: classOf[Double] :: classOf[Float] :: Nil
	private val BoolTypes = classOf[Boolean] :: classOf[java.lang.Boolean] :: Nil

	private def createSql(cls: Class[_ <: JakonObject], annotatedFields: Seq[Field], standalone: Boolean): String = {
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} ")
		if (!standalone) {
			sb.append("(id ")
		}
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
				if (fst != null && Utils.isJakonObject(TypeToken.get(fst).getRawType)) {
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

	def insertStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int | Null, standalone: Boolean): PreparedStatement = {
		val annotatedFields = getJakonFields(instance.getClass)
		val sql = createSql(instance.getClass, annotatedFields, standalone)
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

		if (!standalone) {
			jid match {
				case x: Int => stmt.setInt(1, x)
			}
		}
		filterAndSetValues(instance, annotatedFields, stmt, 2)

		stmt
	}

	private def updateSql(cls: Class[_ <: JakonObject], annotatedFields: Seq[Field], jid: Int): String = {
		val sb = new StringBuilder
		sb.append(s"UPDATE ${cls.getSimpleName} SET ")

		if (annotatedFields.nonEmpty) {
			sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
				annotatedFields.head.getType.getGenericSuperclass.getTypeName == classOf[JakonObject].getName) {
				annotatedFields.head.getName + "_id" + " = ?"
			} else {
				annotatedFields.head.getName + " = ?"
			})

			var embeddedFieldCounter = 0
			annotatedFields.tail.foreach(f => {
				val fst = f.getType.getGenericSuperclass
				if (fst != null && Utils.isJakonObject(TypeToken.get(fst).getRawType)) {
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
		} else {
			sb.append("id = " + jid)
		}
		sb.append(" WHERE id = ?;")
		sb.toString()
	}

	def updateStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields = getJakonFields(instance.getClass)
		val sql = updateSql(instance.getClass, annotatedFields, jid)
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
						if (!field.trySetAccessible()) {
							throw new IllegalStateException(s"Can´t set access for field ${field.getName}")
						}
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
		if (!f.trySetAccessible()) {
			throw new IllegalStateException(s"Can´t set access for field ${f.getName}")
		}

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
			case BIG_DECIMAL_j => stmt.setBigDecimal(i, value.asInstanceOf[java.math.BigDecimal])
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
					if (!Utils.isClassOrChild(converter, classOf[AbstractConverter[_]])) {
						Logger.error(s"Converters are unsupported on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setString(i, "")
					} else if (converter == classOf[NoOpConverter]) {
						Logger.error(s"Convertor not specified for data type on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setNull(i, getSqlType(f))
					} else {
						val c = converter.getDeclaredConstructor().newInstance()
						val methodOpt = converter.getMethods.find(m => m.getName == "convertToDatabaseColumn")
						if (methodOpt.isDefined) {
							stmt.setString(i, methodOpt.get.invoke(c, value).toString)
						}
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
			case BIG_DECIMAL_j => JDBCType.DECIMAL.getVendorTypeNumber
			case DATE_o | DATE => JDBCType.DATE.getVendorTypeNumber
			case DATETIME => JDBCType.TIMESTAMP.getVendorTypeNumber
			case MAP => JDBCType.VARCHAR.getVendorTypeNumber
			case SEQ => JDBCType.VARCHAR.getVendorTypeNumber
			case _ =>
				Logger.error(s"Unknown sql type ${f.getType} on field ${f.getName}")
				0
		}
	}

	/**
	 * SQL WHERE fragment with `?` placeholders and ordered bind values.
	 */
	case class FilterSql(sql: String, params: Seq[Any]) {
		def isEmpty: Boolean = sql.isEmpty
		def nonEmpty: Boolean = sql.nonEmpty

		/** Binds params to statement starting at given index. Returns next free index. */
		def bind(stmt: PreparedStatement, startIndex: Int = 1): Int = {
			var i = startIndex
			params.foreach(p => {
				p match {
					case d: Double => stmt.setDouble(i, d)
					case n: Int => stmt.setInt(i, n)
					case s: String => stmt.setString(i, s)
					case o => stmt.setObject(i, o)
				}
				i += 1
			})
			i
		}
	}

	object FilterSql {
		val Empty: FilterSql = FilterSql("", Nil)
	}

	private def findField(objectClass: Class[_], fieldName: String): Option[(Class[_], Field)] = {
		var cls: Class[_] = objectClass
		while (cls != null) {
			try {
				return Some(cls -> cls.getDeclaredField(fieldName))
			} catch {
				case _: NoSuchFieldException => cls = cls.getSuperclass
			}
		}
		None
	}

	/**
	 * Creates parametrized WHERE clause from filter params. Values are never concatenated into SQL,
	 * field names must match declared fields of objectClass (unknown fields are ignored).
	 */
	def parseFilterParams(kv: mutable.Map[String, String], objectClass: Class[_]): FilterSql = {
		if (kv.isEmpty) {
			return FilterSql.Empty
		}
		val conditions = mutable.ArrayBuffer[String]()
		val params = mutable.ArrayBuffer[Any]()
		for ((fieldName, v) <- kv) {
			findField(objectClass, fieldName) match {
				case None =>
					Logger.warn(s"Ignoring filter on unknown field ${objectClass.getSimpleName}.$fieldName")
				case Some((cls, field)) =>
					val sb = new mutable.StringBuilder()
					sb.append(cls.getSimpleName)
					sb.append(".")
					sb.append(field.getName)
					if (classOf[JakonObject].isAssignableFrom(field.getType)) {
						sb.append("_id")
					}

					val value = v.trim.toLowerCase
					if (value.contains("*")) {
						sb.append(" LIKE ?")
						params += value.replace("*", "%")
					} else {
						sb.append(" = ?")
						if (NumberTypes.contains(field.getType)) {
							params += value.toDoubleOption.getOrElse(value)
						} else if (BoolTypes.contains(field.getType)) {
							params += value.toBooleanOption.map(b => if (b) 1 else 0).getOrElse(value)
						} else {
							params += value
						}
					}
					conditions += sb.toString()
			}
		}
		if (conditions.isEmpty) {
			FilterSql.Empty
		} else {
			FilterSql(conditions.mkString("WHERE ", " AND ", ""), params.toSeq)
		}
	}

}
