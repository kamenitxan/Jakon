package cz.kamenitxan.jakon.webui.functions

import java.lang.reflect.Field
import java.util

import com.mitchellbosecke.pebble.extension.Function
import cz.kamenitxan.jakon.utils.Utils

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

	override def execute(args: util.Map[String, Object]): Object = {
		val attrName = args.get("attr").asInstanceOf[String]

		val obj = args.get("object")
		val field = getField(obj.getClass, attrName)
		field.setAccessible(true)
		field.get(obj)
	}

	def getField(obj: Class[_ <: Any], attrName: String): Field = {
		try {
			val fields = Utils.getFieldsUpTo(obj, classOf[Object])
			fields.find(f => attrName.equals(f.getName)).get
		} catch {
			case e: NoSuchFieldException => getField(obj.getSuperclass, attrName)
		}
	}

}