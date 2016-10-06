package cz.kamenitxan.jakon.utils

import java.util

import com.mitchellbosecke.pebble.extension.Function

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
		val field = obj.getClass.getDeclaredField(attrName)
		field.setAccessible(true)
		field.get(obj)
	}
}