package cz.kamenitxan.jakon.webui.functions

import java.io.File
import java.net.URLClassLoader
import java.text.MessageFormat
import java.util
import java.util.{Locale, MissingResourceException, ResourceBundle}

import com.mitchellbosecke.pebble.extension.i18n.{UTF8Control, i18nFunction}
import com.mitchellbosecke.pebble.template.EvaluationContext
import cz.kamenitxan.jakon.core.configuration.Settings

class i18nFun extends i18nFunction {

	getArgumentNames.add("def")

	override def execute(args: util.Map[String, AnyRef]): AnyRef = {
		val basename = args.get("bundle").asInstanceOf[String]
		val key = args.get("key").asInstanceOf[String]
		val params = args.get("params")
		val default = args.get("def").asInstanceOf[String]

		val context = args.get("_context").asInstanceOf[EvaluationContext]
		val locale = if (Settings.getDefaultLocale == null) context.getLocale else Settings.getDefaultLocale

		val file = new File("templates/admin/")
		val urls = Array(file.toURI.toURL)
		val loader = new URLClassLoader(urls)

		val bundle = ResourceBundle.getBundle(basename, locale, loader, new UTF8Control)
		var phraseObject = ""
		try {
			if (bundle.containsKey(key)) {
				phraseObject = bundle.getString(key)
			} else {
				phraseObject = bundle.getString(default)
			}
		} catch {
			case _: MissingResourceException => {
				val bundle = ResourceBundle.getBundle(basename, new Locale("en", "US"), loader, new UTF8Control)
				try {
					if (bundle.containsKey(key)) {
						phraseObject = bundle.getString(key)
					} else {
						phraseObject = bundle.getString(default)
					}
				} catch {
					case _: MissingResourceException => phraseObject = if (default != null && !default.isEmpty) default else key
				}
			}
		}

		if (phraseObject != null && params != null) {
			params match {
				case list: java.util.List[_] => return MessageFormat.format(phraseObject, list.toArray)
				case _ => return MessageFormat.format(phraseObject, params)
			}
		}
		phraseObject
	}
}
