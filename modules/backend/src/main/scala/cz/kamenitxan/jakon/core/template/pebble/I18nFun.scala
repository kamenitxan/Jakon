package cz.kamenitxan.jakon.core.template.pebble

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.utils.{I18nUtil, PageContext}
import io.pebbletemplates.pebble.extension.i18n.i18nFunction
import io.pebbletemplates.pebble.template.{EvaluationContext, PebbleTemplate}

import java.text.MessageFormat
import java.util
import java.util.Locale

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
		lazy val contextLocale = context.getVariable(I18nFun.renderLocale)

		val evaluationContext = args.get("_context").asInstanceOf[EvaluationContext] // TODO: je to tu potreba? nestaci promena context?
		val lu = if (PageContext.getInstance() != null) PageContext.getInstance().getLoggedUser else null
		val locale = if (lu != null && lu.nonEmpty && lu.get.locale != null) {
			lu.get.locale
		} else if (contextLocale != null && contextLocale.isInstanceOf[Locale]) {
			contextLocale.asInstanceOf[Locale]
		} else if (Settings.getDefaultLocale != null) {
			Settings.getDefaultLocale
		} else {
			evaluationContext.getLocale
		}

		val phraseObject = I18nUtil.getTranslation(templateDir, basename, key, locale, default, silent)
		if (phraseObject != null && params != null && params.nonEmpty) {
			val paramsArray = params.toArray
			return MessageFormat.format(phraseObject, paramsArray:_*)
		}
		phraseObject
	}

}

object I18nFun {
	val renderLocale = "jakon_render_locale"
}

