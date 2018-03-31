package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import spark.ModelAndView

import scala.collection.JavaConverters._
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
		val modelClasses = DBHelper.getDaoClasses.map(_.getSimpleName).asJavaCollection
		val context = Map[String, Any](
			"modelClasses" -> modelClasses,
			"objectSettings" -> DBHelper.getDaoClasses.map(o => (o.getSimpleName, o.newInstance().getObjectSettings)).toMap.asJava,
			"enableFiles" -> AdminSettings.enableFiles,
			"customControllers" -> AdminSettings.customControllers.map(c => c.newInstance()).asJava
		)
		context
	}

	override def getModel: AnyRef = {
		val map = new java.util.HashMap[String, Any]
		if (model != null) {
			model.foreach { p => {
				map.put(p._1, p._2 match {
					case list: List[Any] => list.asJava
					case map: Map[Any, Any] => map.asJava
					case _ => p._2
				})
			}
			}
		}
		map
	}
}
