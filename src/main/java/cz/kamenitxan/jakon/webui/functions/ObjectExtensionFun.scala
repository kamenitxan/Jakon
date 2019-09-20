package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.AdminSettings

import scala.collection.mutable

class ObjectExtensionFun extends Function {

	override def execute(args: util.Map[String, AnyRef]): AnyRef = {
		val obj = args.get("object").asInstanceOf[JakonObject]
		val oes = AdminSettings.objectExtensions.filter(oe => oe.getClass.getCanonicalName == obj.getClass.getCanonicalName)
		if (oepOpt.isDefined) {
			val oep = oepOpt.get
			oep.render(
				mutable.Map[String, Any](
					"objekt" -> obj
				), obj.getClass.getSimpleName, null
			)
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
