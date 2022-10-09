package cz.kamenitxan.jakon.core.template.pebble

import com.mitchellbosecke.pebble.extension.i18n.i18nFunction
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.utils.{I18nUtil, PageContext}

import java.text.MessageFormat
import java.util

class I18nFun extends i18nFunction {
	getArgumentNames.add("def")
	getArgumentNames.add("s")

	val templateDir: String = Settings.getTemplateDir

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val basename = args.get("bundle").asInstanceOf[String]
		val key = args.get("key").asInstanceOf[String]
		val params = args.get("params").asInstanceOf[Seq[String]]
		val default = args.get("def").asInstanceOf[String]
		val silentArg = args.get("s").asInstanceOf[String]
		val silent = if (silentArg == null) false else true

		val context = args.get("_context").asInstanceOf[EvaluationContext]
		val lu = if (PageContext.getInstance() != null) PageContext.getInstance().getLoggedUser else null
		val locale = if (lu != null && lu.nonEmpty && lu.get.locale != null) {
			lu.get.locale
		} else if (Settings.getDefaultLocale != null) {
			Settings.getDefaultLocale
		} else {
			context.getLocale
		}

		val phraseObject = I18nUtil.getTranslation(templateDir, basename, key, locale, default, silent)
		if (phraseObject != null && params != null && params.nonEmpty) {
			val paramsArray = params.toArray
			return MessageFormat.format(phraseObject, paramsArray:_*)
		}
		phraseObject
	}

}

