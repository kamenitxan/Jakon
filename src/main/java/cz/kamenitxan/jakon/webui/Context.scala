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
	}

	def getAdminContext: Map[String, Any] = {
		val modelClasses = DBHelper.getDaoClasses.map(_.getSimpleName).asJavaCollection
		val context = Map[String, Any](
			"modelClasses" -> modelClasses
		)
		context
	}

	override def getModel: AnyRef = {
		val map = new java.util.HashMap[String, Any]
		if (model != null) {
			model.foreach { p => map.put(p._1, p._2) }
		}
		map
	}
}
