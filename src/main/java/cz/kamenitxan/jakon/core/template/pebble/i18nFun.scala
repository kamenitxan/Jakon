package cz.kamenitxan.jakon.core.template.pebble

import java.io.File
import java.net.URLClassLoader
import java.text.MessageFormat
import java.util
import java.util.{Locale, MissingResourceException, ResourceBundle}

import com.mitchellbosecke.pebble.extension.i18n.{UTF8Control, i18nFunction}
import com.mitchellbosecke.pebble.template.{EvaluationContext, PebbleTemplate}
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.logging.Logger

class i18nFun extends i18nFunction {
	getArgumentNames.add("def")

	val templateDir: String = Settings.getTemplateDir

	override def execute(args: util.Map[String, AnyRef], self: PebbleTemplate, context: EvaluationContext, lineNumber: Int): AnyRef = {
		val basename = args.get("bundle").asInstanceOf[String]
		val key = args.get("key").asInstanceOf[String]
		val params = args.get("params").asInstanceOf[Seq[String]]
		val default = args.get("def").asInstanceOf[String]

		val context = args.get("_context").asInstanceOf[EvaluationContext]
		val locale = if (Settings.getDefaultLocale == null) context.getLocale else Settings.getDefaultLocale

		var phraseObject = ""
		try {
			val bundle = getBundle(basename, locale)
			if (bundle.containsKey(key)) {
				phraseObject = bundle.getString(key)
			} else if (default != null) {
				phraseObject = bundle.getString(default)
			} else {
				throw new MissingResourceException("", "", "")
			}
		} catch {
			case _: MissingResourceException => {
				try {
					val bundle = getBundle(basename, new Locale("en", "US"))
					if (bundle.containsKey(key)) {
						phraseObject = bundle.getString(key)
					} else if (default != null) {
						phraseObject = bundle.getString(default)
					} else {
						throw new MissingResourceException("", "", "")
					}
				} catch {
					case _: MissingResourceException =>
						Logger.warn(s"Translation not found for key: $basename.$key")
						phraseObject = if (default != null && !default.isEmpty) default else key
				}
			}
		}

		if (phraseObject != null && params != null && params.nonEmpty) {
			return MessageFormat.format(phraseObject, params.toArray:_*)
		}
		phraseObject
	}

	def getBundle(name: String, locale: Locale): ResourceBundle = {
		val file = new File(templateDir)
		val urls = Array(file.toURI.toURL)
		val loader = new URLClassLoader(urls, null)
		try {
			val bundle = ResourceBundle.getBundle(name, locale, loader, new UTF8Control)
			if (bundle.getLocale == locale) {
				return bundle
			}
		} catch {
			case _: MissingResourceException => // ignored
		}
		val bundle = ResourceBundle.getBundle(templateDir + name, locale, this.getClass.getClassLoader, new UTF8Control)
		if (bundle.getLocale != locale) {
			throw new MissingResourceException("Resource Bundle not found in correct locale", name, "")
		}
		bundle
	}
}

