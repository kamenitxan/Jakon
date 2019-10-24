package cz.kamenitxan.jakon.core.template.pebble

import java.io.File
import java.net.URLClassLoader
import java.text.MessageFormat
import java.util
import java.util.{Locale, MissingResourceException, ResourceBundle}

import com.mitchellbosecke.pebble.extension.i18n.{UTF8Control, i18nFunction}
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.configuration.Settings
import org.slf4j.{Logger, LoggerFactory}

class i18nFun extends i18nFunction {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	getArgumentNames.add("def")

	val templateDir: String = Settings.getTemplateDir

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val basename = args.get("bundle").asInstanceOf[String]
		val key = args.get("key").asInstanceOf[String]
		val params = args.get("params").asInstanceOf[Seq[String]]
		val default = args.get("def").asInstanceOf[String]

		val context = args.get("_context").asInstanceOf[EvaluationContext]
		val locale = if (Settings.getDefaultLocale == null) context.getLocale else Settings.getDefaultLocale

		val file = new File(templateDir)
		val resourceDir = this.getClass.getClassLoader.getResource("templates/admin/")
		val urls = Array(file.toURI.toURL, resourceDir.toURI.toURL)
		val loader = new URLClassLoader(urls, null)

		val bundle = ResourceBundle.getBundle(basename, locale, loader, new UTF8Control)
		var phraseObject = ""
		try {
			if (bundle.containsKey(key)) {
				phraseObject = bundle.getString(key)
			} else if (default != null) {
				phraseObject = bundle.getString(default)
			} else {
				throw new MissingResourceException("", "", "")
			}
		} catch {
			case _: MissingResourceException => {
				val bundle = ResourceBundle.getBundle(basename, new Locale("en", "US"), loader, new UTF8Control)
				try {
					if (bundle.containsKey(key)) {
						phraseObject = bundle.getString(key)
					} else if (default != null) {
						phraseObject = bundle.getString(default)
					} else {
						throw new MissingResourceException("", "", "")
					}
				} catch {
					case _: MissingResourceException =>
						logger.warn(s"Translation not found for key: $key")
						phraseObject = if (default != null && !default.isEmpty) default else key
				}
			}
		}

		if (phraseObject != null && params != null && params.nonEmpty) {
			return MessageFormat.format(phraseObject, params.toArray:_*)
		}
		phraseObject
	}
}
