package cz.kamenitxan.jakon.core.function

import java.util
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.BasicJakonObject

import java.sql.Connection

/**
  * Created by TPa on 25.05.16.
  */
class Link extends IFuncion {

	def execute(params: util.Map[String, String]): String = {
		if (params == null) throw new IllegalArgumentException("Link parameters null")
		val objectId = Integer.valueOf(params.getOrDefault("id", null))
		val target = params.getOrDefault("target", null)
		val text = params.getOrDefault("text", null)
		if (objectId == null || text == null) {
			throw new IllegalArgumentException("Invalid link parameters")
		}

		implicit val conn: Connection = DBHelper.getConnection
		try {
			val stmt = conn.prepareStatement("SELECT id, url FROM JakonObject WHERE id = ?")
			stmt.setInt(1, objectId)
			val qr = DBHelper.selectSingle(stmt, classOf[BasicJakonObject])
			if (qr.entity == null) {
				return ""
			}
			val jakonObject = qr.entity
			val sb = new StringBuilder
			sb.append("<a href=\"")
			sb.append(jakonObject.url)
			sb.append("\" ")
			if (target != null) {
				sb.append("target=\"").append(target).append("\" ")
			}
			sb.append(">").append(text).append("</a>")
			sb.toString
		} finally {
			conn.close()
		}
	}
}