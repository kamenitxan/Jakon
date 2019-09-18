package cz.kamenitxan.jakon.webui.functions

import java.util

import com.mitchellbosecke.pebble.extension.Function
import cz.kamenitxan.jakon.webui.entity.Message

import scala.collection.JavaConverters._
import scala.collection.convert.Wrappers


/**
  * Created by TPa on 6.10.16.
  */
class SplitMessagesFun extends Function {
	def getArgumentNames: util.List[String] = {
		val names = new util.ArrayList[String]
		names.add("messages")
		names
	}

	override def execute(args: util.Map[String, Object]): Object = {
		val messages = args.get("messages").asInstanceOf[Wrappers.MutableSeqWrapper[Message]].underlying
		val map = new util.HashMap[String, util.ArrayList[Message]]()
		messages.groupBy(m => m._severity).foreach(g => map.put(g._1.value, new util.ArrayList[Message](g._2.asJava)))
		map
	}



}