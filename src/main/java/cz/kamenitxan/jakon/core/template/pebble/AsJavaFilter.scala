package cz.kamenitxan.jakon.core.template.pebble

import java.util

import com.mitchellbosecke.pebble.`extension`.Filter
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.configuration.Settings

import scala.jdk.CollectionConverters._

/**
 * Created by TPa on 30.11.2020.
 */
class AsJavaFilter extends Filter {

	val templateDir: String = Settings.getTemplateDir

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any = {
		if (input == null) return null
		input match {
			case value: Seq[Any] =>
				value.asJava
			case _ =>
				input
		}
	}

	override def getArgumentNames: util.List[String] = null
}
