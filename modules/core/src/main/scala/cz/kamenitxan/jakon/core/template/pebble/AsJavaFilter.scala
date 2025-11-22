package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.core.configuration.Settings
import io.pebbletemplates.pebble.`extension`.Filter
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util
import scala.jdk.CollectionConverters.*

/**
 * Created by TPa on 30.11.2020.
 */
class AsJavaFilter extends Filter {

	val templateDir: String = Settings.getTemplateDir

	override def apply(input: Any, args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): Any = {
		if (input == null) return null
		input match {
			case value: Seq[_] =>
				value.asJava
			case value: Map[_, _] =>
				value.asJava
			case _ =>
				input
		}
	}

	override def getArgumentNames: util.List[String] = null
}
