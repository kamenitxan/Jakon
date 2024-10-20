package cz.kamenitxan.jakon.core.template.utils

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.pebble.JakonMethodAccessValidator
import cz.kamenitxan.jakon.webui.functions.AdminPebbleExtension
import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.loader.Loader
import cz.kamenitxan.jakon.webui.ModelAndView
import io.javalin.http.Context
import io.javalin.rendering.FileRenderer

import java.io.{IOException, StringWriter}
import java.util

/**
  * Template Engine based on Pebble.
  */
class JakonPebbleTemplateEngine extends FileRenderer {
	var engine: PebbleEngine = _

	/**
	  * Construct a new template engine using pebble with an engine using a special loader.
	  */
	def this(loader: Loader[_]) = {
		this()
		val builder = new PebbleEngine.Builder()
			.loader(loader)
			.extension(new AdminPebbleExtension)
			.methodAccessValidator(new JakonMethodAccessValidator)
		if (DeployMode.PRODUCTION != Settings.getDeployMode) {
			builder.templateCache(null)
			builder.tagCache(null)
			builder.cacheActive(false)
		}
		this.engine = builder.build
	}

	override def render(s: String, map: util.Map[String, _], context: Context): String = ??? // TODO JAVALIN FIX

	@SuppressWarnings(Array("unchecked"))
	def render(modelAndView: ModelAndView): String = {
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