package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.sql.{Connection, PreparedStatement, Statement}

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.core.model.converters.AbstractConverter
import cz.kamenitxan.jakon.utils.TypeReferences.{B, D, I, S}
import cz.kamenitxan.jakon.webui.entity.JakonField
import javax.persistence.ManyToOne
import org.slf4j.{Logger, LoggerFactory}


object SqlGen {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	private def createSql(cls: Class[_ <: JakonObject], annotatedFields: Array[Field]): String = {
		val annotatedFields = getJakonFields(cls)
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} (")
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
		sb.append(") VALUES (?")
		annotatedFields.tail.foreach(f => sb.append(", ?"))
		sb.append(");")
		println(sb.toString())
		sb.toString()
	}

	def insertStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields: Array[Field] = getJakonFields(instance.getClass)
		val sql = createSql(instance.getClass, annotatedFields)
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

		for ((field, i) <- annotatedFields.toSeq.view.zip(Stream.from(1))) {
			setValue(stmt, field, i, instance)
		}

		stmt
	}

	private def updateSql(cls: Class[_ <: JakonObject], annotatedFields: Array[Field]): String = {
		val sb = new StringBuilder
		sb.append(s"UPDATE ${cls.getSimpleName} SET ")

		sb.append(if (annotatedFields.head.getType.getGenericSuperclass != null &&
		  annotatedFields.head.getType.getGenericSuperclass.getTypeName == classOf[JakonObject].getName) {
			annotatedFields.head.getName + "_id" + " = ?"
		} else {
			annotatedFields.head.getName + " ?"
		})

		annotatedFields.tail.foreach(f => {
			val fst = f.getType.getGenericSuperclass
			if (fst != null && fst.getTypeName == classOf[JakonObject].getName) {
				sb.append(", " + f.getName + "_id + ?")
			} else {
				sb.append(", " + f.getName + " ?")
			}
		})
		sb.append(" WHERE id = ?;")
		sb.toString()
	}

	def updateStmt[T <: JakonObject](instance: T, conn: Connection, jid: Int): PreparedStatement = {
		val annotatedFields: Array[Field] = getJakonFields(instance.getClass)
		val sql = updateSql(instance.getClass, annotatedFields)
		val stmt = conn.prepareStatement(sql)

		for ((field, i) <- annotatedFields.toSeq.view.zip(Stream.from(1))) {
			setValue(stmt, field, i, instance)
		}

		stmt
	}


	private def getJakonFields(cls: Class[_ <: JakonObject]): Array[Field] = {
		cls.getDeclaredFields.filter(f => f.getAnnotations.exists(fa => fa.annotationType().getName == classOf[JakonField].getName))
	}

	private def setValue[T <: JakonObject](stmt: PreparedStatement, f: Field, i: Int, inst: T) = {
		f.getType match {
			case S => stmt.setString(i, f.get(inst).asInstanceOf[String])
			case B => stmt.setBoolean(i, f.get(inst).asInstanceOf[Boolean])
			case I => stmt.setInt(i, f.get(inst).asInstanceOf[Int])
			case D => stmt.setDouble(i, f.get(inst).asInstanceOf[Double])
			case x if x.isEnum =>
				val enumValue = f.get(inst)
				val nameMethod = enumValue.getClass.getDeclaredMethod("name")
				stmt.setString(i, nameMethod.invoke(enumValue).toString)
			case _ =>
				val manyToOne = f.getAnnotation(classOf[ManyToOne])
				lazy val jakonField = f.getAnnotation(classOf[JakonField])
				if (manyToOne != null) {
					stmt.setInt(i, f.get(inst).asInstanceOf[Int])
				} else if (jakonField != null) {
					val converter = jakonField.converter()
					if (converter.getName != classOf[AbstractConverter[_]].getName) {
						logger.error(s"Converters are unsupported on ${inst.getClass.getSimpleName}.${f.getName}")
						stmt.setString(i, "")
					}
				} else {
					logger.warn(s"Uknown data type on ${inst.getClass.getSimpleName}.${f.getName}")
				}
		}
	}

}
