package cz.kamenitxan.jakon.core.template.pebble

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

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

