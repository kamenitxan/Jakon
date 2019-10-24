package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{BasicJakonObject, JakonObject}


class LinkFun extends Function {
	val URL_BY_ID_SQL = "SELECT url FROM JakonObject WHERE id = ?"

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val id = args.get("id").asInstanceOf[Long].toInt
		val conn = DBHelper.getConnection
		try {
			val stmt = conn.prepareStatement(URL_BY_ID_SQL)
			stmt.setInt(1, id)
			val jo = DBHelper.selectSingle(stmt, classOf[BasicJakonObject]).entity.asInstanceOf[JakonObject]
			if (jo == null) {
				return ""
			}
			"<a href=\"" + jo.createUrl + ".html\">" + jo.createUrl + "</a>"
		} finally {
			conn.close()
		}
	}

	override def getArgumentNames: util.ArrayList[String] = {
		val names = new util.ArrayList[String]
		names.add("id")
		names
	}
}
