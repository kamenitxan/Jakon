package cz.kamenitxan.jakon.core.template.pebble

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util
import scala.jdk.CollectionConverters.*

/**
  * @return first not null argument
  */
class ValueFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		args.values().asScala.find(a => a != null).orNull
	}

	override def getArgumentNames: util.List[String] = null
}

