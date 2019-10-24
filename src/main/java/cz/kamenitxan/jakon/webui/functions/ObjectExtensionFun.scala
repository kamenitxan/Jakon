package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.AdminSettings

import scala.collection.mutable

class ObjectExtensionFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val obj = args.get("object").asInstanceOf[JakonObject]
		val result = AdminSettings.objectExtensions
		  .filter(oe => oe._1.getCanonicalName == obj.getClass.getCanonicalName)
		  .flatMap(oe => oe._2)
		  .map(oe => {
			  oe.newInstance().render(
				  mutable.Map[String, Any](
					  "object" -> obj
				  ), "objects/extension/" + obj.getClass.getSimpleName, PageContext.getInstance().req
			  )
		  })
		if (result.nonEmpty) {
			"<hr>" + result.mkString
		} else {
			""
		}

	}

	override def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add("object")
		names
	}
}
