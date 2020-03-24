package cz.kamenitxan.jakon.core.template

import java.io.StringWriter
import java.util

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.loader.StringLoader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.devtools.DevRender
import cz.kamenitxan.jakon.webui.util.JakonFileLoader

import scala.collection.JavaConverters._


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Pebble extends TemplateEngine {
	private val loader = new JakonFileLoader(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	private val builder = new PebbleEngine.Builder()
	builder.loader(loader)
	builder.extension(new PebbleExtension)
	builder.strictVariables(true)
	if (DeployMode.PRODUCTION != Settings.getDeployMode) {
		builder.templateCache(null)
		builder.tagCache(null)
		builder.cacheActive(false)
	}
	private val engine = builder.build()
	private val stringEngine = builder.loader(new StringLoader()).build()

	def render(templateName: String, path: String, context: util.Map[String, AnyRef])(implicit caller: IController) {

		val output = renderString(templateName, context)
		TemplateUtils.saveRenderedPage(output, path)
		if (Settings.getDeployMode != DeployMode.PRODUCTION) {
			DevRender.registerPath(path, caller)
		}
	}

	override def renderToString(templateName: String, context: Map[String, AnyRef]): String = {
		renderString(templateName, mapAsJavaMap(context))
	}

	private def renderString(templateName: String, context: util.Map[String, AnyRef]): String = {
		var compiledTemplate: PebbleTemplate = null
		try {
			compiledTemplate = engine.getTemplate(templateName)
		} catch {
			case e: PebbleException => e.printStackTrace()
		}
		val writer = new StringWriter
		try {
			if (compiledTemplate != null) {
				compiledTemplate.evaluate(writer, context)
			}
		} catch {
			case e: Any => e.printStackTrace()
		}
		writer.toString
	}

	override def renderTemplate(template: String, context: Map[String, AnyRef]): String = {
		val writer = new StringWriter()
		stringEngine.getTemplate(template).evaluate(writer, mapAsJavaMap(context))
		writer.toString
	}
}