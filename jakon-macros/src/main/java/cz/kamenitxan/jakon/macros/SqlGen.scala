package cz.kamenitxan.jakon.macros

import java.sql.{Connection, Statement}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox


object SqlGen {

	def insert(cls: Class[_ <: AnyRef], conn: Connection): Statement = macro insert_base

	def insert_base(c: blackbox.Context)(cls: c.Tree, conn: c.Tree): c.Tree = {
		import c.universe._

		q"SqlGen.insert_impl($cls, $conn)"

	}

	def insert_impl(cls: Class[_ <: AnyRef], conn: Connection): Statement = {
		val annotatedFields = cls.getDeclaredFields.filter(f => f.getAnnotations.exists(fa => fa.annotationType().getName == "cz.kamenitxan.jakon.webui.entity.JakonField"))
		val sb = new StringBuilder
		sb.append(s"INSERT INTO ${cls.getSimpleName} (")
		sb.append(if(annotatedFields.head.getType.getName == "cz.kamenitxan.jakon.model.JakonObject") {
			annotatedFields.head.getName + "_id"
		} else {
			annotatedFields.head.getName
		})

		annotatedFields.tail.foreach(f => if(f.getType.getName == "cz.kamenitxan.jakon.model.JakonObject") {
			sb.append(", " +f.getName + "_id")
		} else {
			sb.append(", "+ f.getName)
		})
		sb.append(") VALUES (?")
		annotatedFields.tail.foreach(f => sb.append(", ?"))
		sb.append(");")
		println(sb.toString())
		val stmt = conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS)

		stmt
	}

}
