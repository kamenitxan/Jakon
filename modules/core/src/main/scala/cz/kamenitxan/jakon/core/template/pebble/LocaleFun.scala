package cz.kamenitxan.jakon.core.template.pebble

import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.util

/**
  * @return code of the language of the UI (e.g. "cs"), the same locale that i18n() translates to
  */
class LocaleFun extends Function {

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		I18nFun.resolveLocale(context).getLanguage
	}

	override def getArgumentNames: util.List[String] = null
}
