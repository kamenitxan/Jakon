package cz.kamenitxan.jakon.webui.functions

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controller.objectextension.{ExtensionType, ObjectExtension}
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util
import scala.collection.mutable

class ObjectExtensionFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val obj = args.get(ObjectExtensionFun.PARAM_OBJECT).asInstanceOf[JakonObject]
		val extensionType = ExtensionType.valueOf(args.get(ObjectExtensionFun.PARAM_TYPE).asInstanceOf[String])
		val result = AdminSettings.objectExtensions
			.filter(oe => oe._1.getCanonicalName == obj.getClass.getCanonicalName)
			.flatMap(oe => oe._2)
			.filter(oe => {
				val et = oe.getDeclaredAnnotation(classOf[ObjectExtension]).extensionType()
				et == extensionType || et == ExtensionType.BOTH
			})
			.map(oe => {
				val instance = oe.getDeclaredConstructor().newInstance()
				"<div class=\"extension_holder container-fluid\">" + instance.render(
					mutable.Map[String, Any](
						"object" -> obj
					), "objects/extension/" + instance.getClass.getSimpleName, PageContext.getInstance().req
				) + "</div>"
			})
		if (result.nonEmpty) {
			"<hr>" + result.mkString
		} else {
			""
		}

	}

	override def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add(ObjectExtensionFun.PARAM_OBJECT)
		names.add(ObjectExtensionFun.PARAM_TYPE)
		names
	}
}

object ObjectExtensionFun {
	val PARAM_TYPE = "type"
	val PARAM_OBJECT = "object"
}
