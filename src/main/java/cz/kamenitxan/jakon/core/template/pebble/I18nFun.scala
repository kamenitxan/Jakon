package cz.kamenitxan.jakon.core.template.pebble

import java.text.MessageFormat
import java.util
import com.mitchellbosecke.pebble.extension.i18n.i18nFunction
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.utils.{I18nUtil, PageContext}

class I18nFun extends i18nFunction {
	getArgumentNames.add("def")

	val templateDir: String = Settings.getTemplateDir

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val basename = args.get("bundle").asInstanceOf[String]
		val key = args.get("key").asInstanceOf[String]
		val params = args.get("params").asInstanceOf[Seq[String]]
		val default = args.get("def").asInstanceOf[String]

		val context = args.get("_context").asInstanceOf[EvaluationContext]
		val locale = if (PageContext.getInstance().getLoggedUser.nonEmpty) {
			PageContext.getInstance().getLoggedUser.get.locale
		} else if (Settings.getDefaultLocale != null) {
			Settings.getDefaultLocale
		} else {
			context.getLocale
		}

		val phraseObject = I18nUtil.getTranslation(templateDir, basename, key, locale, default)
		if (phraseObject != null && params != null && params.nonEmpty) {
			return MessageFormat.format(phraseObject, params.toArray:_*)
		}
		phraseObject
	}

}

