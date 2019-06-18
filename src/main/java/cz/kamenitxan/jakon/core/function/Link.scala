package cz.kamenitxan.jakon.core.function

import java.util

import cz.kamenitxan.jakon.core.model.BasicJakonObject
import cz.kamenitxan.jakon.core.model.Dao.DBHelper

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
		val jakonObject = new BasicJakonObject //TODO FIX
		if (jakonObject == null) return ""
		val sb = new StringBuilder
		sb.append("<a href=\"")
		sb.append(jakonObject.getUrl)
		sb.append("\" ")
		if (target != null) {
			sb.append("target=\"").append(target).append("\" ")
		}
		sb.append(">").append(text).append("</a>")
		sb.toString
	}
}