package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonObject, JakonUser}


class LinkFun extends Function{
	override def execute(args: util.Map[String, AnyRef]): Object = {
		val id = args.get("id").asInstanceOf[Long].toInt
		val session = DBHelper.getSession
		session.beginTransaction()
		val jo = session.get(classOf[JakonObject], id)
		session.getTransaction.commit()
		if (jo == null) {
			return ""
		}
		"<a href=\"" + jo.getUrl + ".html\">" + jo.getUrl + "</a>"
	}

	override def getArgumentNames: util.ArrayList[String] = {
		val names = new util.ArrayList[String]
		names.add("id")
		names
	}
}
