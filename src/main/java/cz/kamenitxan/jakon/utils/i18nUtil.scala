package cz.kamenitxan.jakon.utils

import java.io.File
import java.net.URLClassLoader
import java.util.{Locale, MissingResourceException, ResourceBundle}

import com.mitchellbosecke.pebble.extension.i18n.UTF8Control
import cz.kamenitxan.jakon.logging.Logger
import Utils._

object i18nUtil {

	def getTranslation(templateDir: String, basename: String, key: String, locale: Locale): String = {
		getTranslation(templateDir, basename, key, locale, null)
	}

	def getTranslation(templateDir: String, basename: String, key: String, locale: Locale, default: String): String = {
		val bundles = getBundles(templateDir, basename, locale)
		if (bundles.isEmpty) {
			Logger.warn(s"Translation bundle not found. basename=$basename, locale=$locale")
			return if (default.isNullOrEmpty) key else default
		}

		val result = bundles.view
		  .map(fetchFromBundle(_, key))
		  .find(_.isDefined)
		  .flatten

		if (result.isDefined) {
			result.get
		} else {
			Logger.warn(s"Translation not found for key: $basename.$key")
			if (default.isNullOrEmpty) key else default
		}
	}

	private def fetchFromBundle(bundle: ResourceBundle, key: String): Option[String] = {
		if (bundle.containsKey(key)) {
			Option.apply(bundle.getString(key))
		} else {
			Option.empty
		}
	}

	private def getBundles(templateDir: String, bundleName: String, locale: Locale): Seq[ResourceBundle] = {
		val file = new File(templateDir)
		val urls = Array(file.toURI.toURL)
		val loader = new URLClassLoader(urls, null)
		val jarLoader = this.getClass.getClassLoader
		val enLocale = new Locale("en", "US")
		Seq(
			(bundleName, locale, loader),
			(templateDir + bundleName, locale, jarLoader),
			(bundleName, enLocale, loader),
			(templateDir + bundleName, enLocale, jarLoader)
		).flatMap(t => getBundle(t._1, t._2, t._3))
	}

	def getBundle(bundleName: String, locale: Locale, loader: ClassLoader): Option[ResourceBundle] = {
		try {
			val bundle = ResourceBundle.getBundle(bundleName, locale, loader, new UTF8Control)
			if (bundle.getLocale == locale) {
				Option(bundle)
			} else {
				Option.empty
			}
		} catch {
			case _: MissingResourceException => Option.empty
		}
	}
}
