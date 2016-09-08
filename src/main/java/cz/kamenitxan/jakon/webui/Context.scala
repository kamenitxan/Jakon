package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import spark.ModelAndView

import collection.JavaConversions._

/**
  * Created by TPa on 06.09.16.
  */
class Context(var model: Map[String, Any], viewName: String) extends ModelAndView(model, viewName) {
	if (model != null) {
		model = model ++ getAdminContext
	}

	def getAdminContext: Map[String, Any] = {
		val modelClasses = asJavaCollection(DBHelper.getDaoClasses.map(_.getSimpleName.toLowerCase()))
		val context = Map[String, Any](
			"modelClasses" -> modelClasses
		)
		context
	}

	override def getModel: AnyRef = mapAsJavaMap(model)
}
