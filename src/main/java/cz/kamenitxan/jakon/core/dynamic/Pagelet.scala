package cz.kamenitxan.jakon.core.dynamic

import java.util

import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.Settings
import spark.{ModelAndView, TemplateViewRoute}
import spark.template.pebble.PebbleTemplateEngine

import scala.collection.JavaConverters._


/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class Pagelet extends TemplateViewRoute{
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	val engine = new PebbleTemplateEngine(loader)

	def render(context: util.Map[String, AnyRef], templatePath: String): String = engine.render(new ModelAndView(context, templatePath))

	def render(context: Map[String, AnyRef], templatePath: String): String = engine.render(new ModelAndView(context.asJava, templatePath))
}
