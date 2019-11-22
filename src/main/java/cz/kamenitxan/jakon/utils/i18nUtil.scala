package cz.kamenitxan.jakon.utils

import java.io.File
import java.net.URLClassLoader
import java.util.{Locale, MissingResourceException, ResourceBundle}

import com.mitchellbosecke.pebble.extension.i18n.UTF8Control
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.logging.Logger

object i18nUtil {

	def getTranslation(basename: String, key: String, locale: Locale): String = {
		getTranslation(basename, key, locale, null)
	}

	def getTranslation(basename: String, key: String, locale: Locale, default: String): String = {
		val file = new File(Settings.getTemplateDir)
		val resourceDir = this.getClass.getClassLoader.getResource("templates/admin/")
		val urls = Array(file.toURI.toURL)
		val loader = new URLClassLoader(urls)

		var bundle: ResourceBundle = null
		var adminBundle: ResourceBundle = null
		try {
			bundle = ResourceBundle.getBundle(basename, locale, loader, new UTF8Control)
		} catch {
			case _: MissingResourceException =>
				Logger.warn(s"Translation bundle not found. basename=$basename, locale=$locale")
				return key
		}
		var phraseObject = ""

		fetchFromBundle(bundle, key)

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
					case _: MissingResourceException => phraseObject = if (default != null && !default.isEmpty) default else key
				}
			}
		}
		phraseObject
	}

	private def fetchFromBundle(bundle: ResourceBundle, key: String): Option[String] = {
		if (bundle.containsKey(key)) {
			Option.apply(bundle.getString(key))
		} else {
			Option.empty
		}
	}
}
