package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.sql.{Connection, JDBCType, PreparedStatement, Statement}
import java.util.Date

import cz.kamenitxan.jakon.core.database.converters.AbstractConverter
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.{ManyToOne, OneToOne, Transient}
import org.slf4j.{Logger, LoggerFactory}


object SqlGen {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	private def createSql(cls: Class[_ <: JakonObject], annotatedFields: List[Field]): String = {
		val annotatedFields = getJakonFields(cls)
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} (id, ")
		sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
		  annotatedFields.head.getType.getGenericSuperclass.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
			annotatedFields.head.getName + "_id"
		} else {
			annotatedFields.head.getName
		})

		annotatedFields.tail.foreach(f => {
			val fst = f.getType.getGenericSuperclass
			if (fst != null && fst.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
				sb.append(", " + f.getName + "_id")
			} else {
				sb.append(", " + f.getName)
			}
		})
		sb.append(") VALUES (?, ?")
		annotatedFields.tail.foreach(_ => sb.append(", ?"))
		sb.append(");")
		logger.trace(s"generated sql: ${sb.toString()}")
		sb.toString()
	}

	def insertStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields = getJakonFields(instance.getClass)
		val sql = createSql(instance.getClass, annotatedFields)
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

		stmt.setInt(1, jid)
		for ((field, i) <- annotatedFields.view.zip(Stream.from(2))) {
			setValue(stmt, field, i, instance)
		}

		stmt
	}

	private def updateSql(cls: Class[_ <: JakonObject], annotatedFields: List[Field]): String = {
		val sb = new StringBuilder
		sb.append(s"UPDATE ${cls.getSimpleName} SET ")

		sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
		  annotatedFields.head.getType.getGenericSuperclass.getTypeName == classOf[JakonObject].getName) {
			annotatedFields.head.getName + "_id" + " = ?"
		} else {
			annotatedFields.head.getName + " = ?"
		})

		annotatedFields.tail.foreach(f => {
			val fst = f.getType.getGenericSuperclass
			if (fst != null && fst.getTypeName == classOf[JakonObject].getName) {
				sb.append(", " + f.getName + "_id = ?")
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

		for ((field, i) <- annotatedFields.view.zip(Stream.from(1))) {
			setValue(stmt, field, i, instance)
		}
		stmt.setInt(annotatedFields.length + 1, jid)

		stmt
	}


	private def getJakonFields(cls: Class[_ <: JakonObject]): List[Field] = {
		val allFields = Utils.getFieldsUpTo(cls, classOf[JakonObject])
		allFields.filter(f =>
			f.getAnnotations.exists(fa => fa.annotationType().getName == classOf[JakonField].getName)
			  && f.getName != "id"
			  && f.getAnnotation(classOf[Transient]) == null
		)
	}

	private def setValue[T <: JakonObject](stmt: PreparedStatement, f: Field, i: Int, inst: T): Unit = {
		if (!f.isAccessible) f.setAccessible(true)

		val value = f.get(inst)
		if (value == null) {
			stmt.setNull(i, getSqlType(f))
			return
		}

		f.getType match {
			case STRING => stmt.setString(i, value.asInstanceOf[String])
			case BOOLEAN => stmt.setBoolean(i, value.asInstanceOf[Boolean])
			case INTEGER => stmt.setInt(i, value.asInstanceOf[Int])
			case LONG => stmt.setLong(i, value.asInstanceOf[Long])
			case DOUBLE => stmt.setDouble(i, value.asInstanceOf[Double])
			case DATE => stmt.setDate(i, new java.sql.Date(value.asInstanceOf[Date].getTime))
			case DATETIME => stmt.setObject(i, value)
			case x if x.isEnum =>
				val nameMethod = value.getClass.getMethod("name")
				stmt.setString(i, nameMethod.invoke(value).toString)
			case _ =>
				lazy val jakonField = f.getAnnotation(classOf[JakonField])
				if (f.getAnnotation(classOf[ManyToOne]) != null || f.getAnnotation(classOf[OneToOne]) != null) {
					stmt.setInt(i, value.asInstanceOf[JakonObject].id)
				} else if (jakonField != null) {
					val converter = jakonField.converter()
					if (converter.getName != classOf[AbstractConverter[_]].getName) {
						logger.error(s"Converters are unsupported on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setString(i, "")
					} else {
						logger.error(s"Convertor not specified for data type on ${inst.getClass.getSimpleName}.${f.getName}")
					}
				} else {
					logger.error(s"Uknown data type on ${inst.getClass.getSimpleName}.${f.getName}")
				}
		}
	}

	def getSqlType(f: Field): Int = {
		f.getType match {
			case x if x.isEnum => JDBCType.VARCHAR.getVendorTypeNumber
			case STRING => JDBCType.VARCHAR.getVendorTypeNumber
			case BOOLEAN => JDBCType.BOOLEAN.getVendorTypeNumber
			case _ if f.getDeclaredAnnotation(classOf[ManyToOne]) != null => JDBCType.INTEGER.getVendorTypeNumber
			case INTEGER => JDBCType.INTEGER.getVendorTypeNumber
			case DOUBLE => JDBCType.DOUBLE.getVendorTypeNumber
			case DATE => JDBCType.DATE.getVendorTypeNumber
			case _ =>
				logger.error(s"Uknown sql type ${f.getType} on field ${f.getName}")
				0
		}
	}

}
