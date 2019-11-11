package cz.kamenitxan.jakon.core.template.pebble

import java.util

import com.mitchellbosecke.pebble.extension.i18n.i18nFunction
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}

import scala.collection.JavaConverters._

/**
  * @return first not null argument
  */
class ValueFun extends i18nFunction {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		args.values().asScala.find(a => a != null).orNull
	}

	override def getArgumentNames: util.List[String] = null
}

