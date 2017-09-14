package cz.kamenitxan.jakon.core.dynamic

import java.util

import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.Settings
import spark.ModelAndView
import spark.template.pebble.PebbleTemplateEngine

/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class Pagelet {
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	val engine = new PebbleTemplateEngine(loader)

	def render(context: util.Map[String, AnyRef], templatePath: String): String = engine.render(new ModelAndView(context, templatePath))
}
