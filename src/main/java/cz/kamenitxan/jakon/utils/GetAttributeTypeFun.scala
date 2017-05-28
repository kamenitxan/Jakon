package cz.kamenitxan.jakon.utils

import java.lang.reflect.Field
import java.util

import com.mitchellbosecke.pebble.extension.Function

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

	override def execute(args: util.Map[String, Object]): Object = {
		val attrName = args.get("attr").asInstanceOf[String]

		val obj = args.get("object")
		val field = getField(obj.getClass, attrName)
		field.setAccessible(true)

		val S = classOf[String]
		val B = classOf[Boolean]

		field.getType match {
			case S => "text"
			case B => "checkbox"
			case _ => "text"
		}
	}

	def getField(obj: Class[_ <: Any], attrName: String): Field = {
		try {
			obj.getDeclaredField(attrName)
		} catch {
			case e: NoSuchFieldException => getField(obj.getSuperclass, attrName)
		}
	}

}