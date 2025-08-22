package cz.kamenitxan.jakon.webui.functions

import cz.kamenitxan.jakon.utils.Utils
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.lang.reflect.Field
import java.util

/**
  * Created by TPa on 6.10.16.
  */
class GetAttributeFun extends Function {
	def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add("object")
		names.add("attr")
		names
	}

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val attrName = args.get("attr").asInstanceOf[String]

		val obj = args.get("object")
		if (obj != null) {
			val field = getField(obj.getClass, attrName)
			if (field == null) {
				return null
			}
			field.setAccessible(true)
			field.get(obj)
		} else {
			null
		}
	}

	def getField(obj: Class[_ <: Any], attrName: String): Field = {
		try {
			val fields = Utils.getFieldsUpTo(obj, classOf[Object])
			val f = fields.find(f => attrName.equals(f.getName))
			if (f.isDefined) {
				f.get
			} else {
				null
			}
		} catch {
			case e: NoSuchFieldException => getField(obj.getSuperclass, attrName)
		}
	}

}