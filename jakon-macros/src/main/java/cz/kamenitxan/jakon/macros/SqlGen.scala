package cz.kamenitxan.jakon.macros

import java.sql.{Connection, Statement}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


object SqlGen {

	def insert(cls: Class[_ <: AnyRef], conn: Connection): Statement = macro insert_base

	def insert_base(c: blackbox.Context)(cls: c.Expr[Class[_ <: AnyRef]], conn: c.Expr[Connection]): c.Tree = {
		import c.universe._

		val sql = q"SqlGen.createSql($cls)"
		q"SqlGen.insert_impl($cls, $conn, $sql)"

	}

	def createSql(cls: Class[_ <: AnyRef]): String = {
		val annotatedFields = cls.getDeclaredFields.filter(f => f.getAnnotations.exists(fa => fa.annotationType().getName == "cz.kamenitxan.jakon.webui.entity.JakonField"))
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} (")
		sb.append(if(annotatedFields.head.getType.getGenericSuperclass !=null &&
		  annotatedFields.head.getType.getGenericSuperclass.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
			annotatedFields.head.getName + "_id"
		} else {
			annotatedFields.head.getName
		})

		annotatedFields.tail.foreach(f => {
			val fst = f.getType.getGenericSuperclass
			if(fst!= null && fst.getTypeName == "cz.kamenitxan.jakon.core.model.JakonObject") {
				sb.append(", " +f.getName + "_id")
			} else {
				sb.append(", "+ f.getName)
			}
		})
		sb.append(") VALUES (?")
		annotatedFields.tail.foreach(f => sb.append(", ?"))
		sb.append(");")
		println(sb.toString())
		sb.toString()
	}

	def insert_impl(cls: Class[_ <: AnyRef], conn: Connection, sql: String): Statement = {
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt
	}

}