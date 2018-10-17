package cz.kamenitxan.jakon.core.template

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.loader.Loader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.configuration.DeployMode
import cz.kamenitxan.jakon.webui.functions.AdminPebbleExtension
import spark.ModelAndView
import spark.TemplateEngine
import java.io.IOException
import java.io.StringWriter
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
		if (DeployMode.DEVEL == Settings.getDeployMode) {
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