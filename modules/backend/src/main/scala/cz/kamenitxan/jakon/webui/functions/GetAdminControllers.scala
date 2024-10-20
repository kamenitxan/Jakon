package cz.kamenitxan.jakon.webui.functions

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.webui.AdminSettings
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util
import scala.jdk.CollectionConverters.*


/**
 * Created by TPa on 18.02.21.
 */
class GetAdminControllers extends Function {
	def getArgumentNames: util.List[String] = null

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val controllers = DBHelper.getDaoClasses.map(_.getCanonicalName) ++ AdminSettings.customControllersInfo.map(_.cls.getCanonicalName)
		controllers.asJava
	}


}