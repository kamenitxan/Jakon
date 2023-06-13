package cz.kamenitxan.jakon.core.template.utils

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.webui.functions.AdminPebbleExtension
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.PebbleException
import io.pebbletemplates.pebble.loader.Loader
import spark.ModelAndView

import java.io.{IOException, StringWriter}
import java.util

/**
  * Template Engine based on Pebble.
  *
  * @author Nikki
  */
class FixedPebbleTemplateEngine() extends spark.TemplateEngine {
	var engine: PebbleEngine = _

	/**
	  * Construct a new template engine using pebble with an engine using a special loader.
	  */
	def this(loader: Loader[_]) = {
		this()
		val builder = new PebbleEngine.Builder().loader(loader).extension(new AdminPebbleExtension)
		if (DeployMode.PRODUCTION != Settings.getDeployMode) {
			builder.templateCache(null)
			builder.tagCache(null)
			builder.cacheActive(false)
		}
		this.engine = builder.build
	}

	@SuppressWarnings(Array("unchecked"))
	override def render(modelAndView: ModelAndView): String = {
		val model = modelAndView.getModel
		if (model == null || model.isInstanceOf[util.Map[_, _]]) try {
			val writer = new StringWriter
			val template = engine.getTemplate(modelAndView.getViewName)
			if (model == null) template.evaluate(writer)
			else template.evaluate(writer, modelAndView.getModel.asInstanceOf[util.Map[String, AnyRef]])
			writer.toString
		} catch {
			case e@(_: PebbleException | _: IOException) =>
				throw new IllegalArgumentException(e)
		}
		else throw new IllegalArgumentException("Invalid model, model must be instance of Map.")
	}


}