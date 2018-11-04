package cz.kamenitxan.jakon.core.dynamic

import java.util

import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.Settings
import spark.{ModelAndView, Request, Response, TemplateViewRoute}
import spark.template.pebble.PebbleTemplateEngine

import scala.collection.JavaConverters._
import scala.collection.mutable


/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class AbstractPagelet extends TemplateViewRoute{
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	val engine = new PebbleTemplateEngine(loader)

	def render(context: util.Map[String, AnyRef], templatePath: String): String = engine.render(new ModelAndView(context, templatePath))

	def render(context: mutable.Map[String, AnyRef], templatePath: String): String = engine.render(new ModelAndView(context.asJava, templatePath))

	def beforeGet(req: Request, res: Response, data: AnyRef): Unit = {

	}

	def afterGet(req: Request, res: Response, data: AnyRef, context: mutable.Map[String, AnyRef]): Unit = {

	}

	def beforePost(req: Request, res: Response, data: AnyRef): Unit = {

	}

	def afterPost(req: Request, res: Response, data: AnyRef, context: mutable.Map[String, AnyRef]): Unit = {

	}
}
