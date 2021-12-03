package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.webui.entity.Message

import scala.jdk.CollectionConverters._


/**
  * Created by TPa on 6.10.16.
  */
class SplitMessagesFun extends Function {
	def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add("messages")
		names
	}

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val messages = args.get("messages").asInstanceOf[java.util.List[Message]].asScala
		val map = new util.HashMap[String, util.ArrayList[Message]]()
		messages.groupBy(m => m._severity).foreach(g => map.put(g._1.value, new util.ArrayList[Message](g._2.asJava)))
		map
	}



}