package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.PageContext

import java.util
import scala.jdk.CollectionConverters.*

/**
 * Created by TPa on 06.09.16.
 */
class Context(var model: Map[String, Any], viewName: String) extends ModelAndView(model, viewName) {
	if (model != null) {
		model = model ++ getAdminContext
	} else {
		model = getAdminContext
	}

	def getAdminContext: Map[String, Any] = {
		Context.getAdminContext
	}

	override def getModel: java.util.HashMap[String, Any] = {
		val map = new java.util.HashMap[String, Any]
		if (model != null) {
			model.foreach { p => {
				map.put(p._1, p._2 match {
					case list: List[_] => list.asJava
					case list: Seq[_] => list.asJava
					case map: Map[_, _] => map.asJava
					case _ => p._2
				})
			}
			}
		}
		map
	}
}

object Context {
	lazy val objectSettings: util.Map[String, ObjectSettings] = DBHelper.getDaoClasses.map(o => (o.getSimpleName, o.getDeclaredConstructor().newInstance().objectSettings)).toMap.asJava

	def getAdminContext: Map[String, Any] = {
		val user = PageContext.getInstance().getLoggedUser
		val allModelClasses = DBHelper.getDaoClasses
			.filter(c => user.nonEmpty && (user.get.acl.masterAdmin || user.get.acl.allowedControllers.contains(c.getCanonicalName)))
			.groupBy(c => c.getPackage.getName.startsWith("cz.kamenitxan.jakon"))
			.view.mapValues(cl => cl.map(c => c.getSimpleName).asJavaCollection)
		val customControllers = AdminSettings.customControllersInfo
			.filter(c => user.nonEmpty && (user.get.acl.masterAdmin || user.get.acl.allowedControllers.contains(c.cls.getCanonicalName)))

		val context = Map[String, Any](
			"user" -> user.orNull,
			"modelClasses" -> allModelClasses.getOrElse(false, new java.util.ArrayList[String]()),
			"jakonModelClasses" -> allModelClasses.getOrElse(true, new java.util.ArrayList[String]()),
			"objectSettings" -> DBHelper.getDaoClasses.map(o => (o.getSimpleName, o.getDeclaredConstructor().newInstance().objectSettings)).toMap.asJava,
			"enableFiles" -> AdminSettings.enableFiles,
			"customControllers" -> customControllers.asJava,
			"jakon_messages" -> PageContext.getInstance().messages.asJava
		)
		context
	}
}
