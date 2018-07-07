package cz.kamenitxan.jakon.core.template

import java.io.StringWriter
import java.util

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
import cz.kamenitxan.jakon.devtools.DevRender

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Pebble extends TemplateEngine {
	private val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	private val builder = new PebbleEngine.Builder()
	builder.loader(loader)
	builder.extension(new PebbleExtension)
	builder.strictVariables(true)
	if (Settings.getDeployMode == DeployMode.DEVEL) {
		builder.templateCache(null)
		builder.tagCache(null)
		builder.cacheActive(false)
	}
	private val engine = builder.build()

	def render(templateName: String, path: String, context: util.Map[String, AnyRef])(implicit caller: IControler) {
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
		val output = writer.toString
		TemplateUtils.saveRenderedPage(output, path)
		if (Settings.getDeployMode == DeployMode.DEVEL) {
			DevRender.registerPath(path, caller)
		}
	}
}