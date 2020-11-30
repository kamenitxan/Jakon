package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.PageContext
import spark.ModelAndView

import scala.jdk.CollectionConverters._

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

	override def getModel: AnyRef = {
		val map = new java.util.HashMap[String, Any]
		if (model != null) {
			model.foreach { p => {
				map.put(p._1, p._2 match {
					case list: List[Any] => list.asJava
					case list: Seq[Any] => list.asJava
					case map: Map[Any, Any] => map.asJava
					case _ => p._2
				})
			}
			}
		}
		map
	}
}

object Context {
	lazy val objectSettings = DBHelper.getDaoClasses.map(o => (o.getSimpleName, o.newInstance().getObjectSettings)).toMap.asJava

	def getAdminContext: Map[String, Any] = {
		val user = PageContext.getInstance().getLoggedUser
		val allModelClasses = DBHelper.getDaoClasses
		  .filter(c => user.nonEmpty && (user.get.acl.masterAdmin || user.get.acl.allowedControllers.contains(c.getCanonicalName)))
		  .groupBy(c => c.getPackage.getName.startsWith("cz.kamenitxan.jakon"))
  		.mapValues(cl => cl.map(c => c.getSimpleName).asJavaCollection)

		val context = Map[String, Any](
			"user" -> user.orNull,
			"modelClasses" -> allModelClasses.getOrElse(false, new java.util.ArrayList[String]()),
			"jakonModelClasses" -> allModelClasses.getOrElse(true, new java.util.ArrayList[String]()),
			"objectSettings" -> DBHelper.getDaoClasses.map(o => (o.getSimpleName, o.newInstance().getObjectSettings)).toMap.asJava,
			"enableFiles" -> AdminSettings.enableFiles,
			"customControllers" -> AdminSettings.customControllersInfo.asJava,
			"jakon_messages" -> PageContext.getInstance().messages.asJava
		)
		context
	}
}
