package cz.kamenitxan.jakon.webui.functions

import java.lang.reflect.Field
import java.util
import java.util.Date

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

/**
  * Created by TPa on 6.10.16.
  */
class GetAttributeTypeFun extends Function {
	def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add("object")
		names.add("attr")
		names
	}

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val attrName = args.get("attr").asInstanceOf[String]

		val obj = args.get("object")
		val field = getField(obj.getClass, attrName)
		field.setAccessible(true)

		val B = classOf[Boolean]
		val DATE = classOf[Date]

		field.getType match {
			case B => "checkbox"
			case DATE => "date"
			case _ => "text"
		}
	}

	def getField(obj: Class[_ <: Any], attrName: String): Field = {
		try {
			obj.getDeclaredField(attrName)
		} catch {
			case _: NoSuchFieldException => getField(obj.getSuperclass, attrName)
		}
	}

}