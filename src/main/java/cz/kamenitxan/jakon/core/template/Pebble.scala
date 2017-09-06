package cz.kamenitxan.jakon.core.template

import java.io.StringWriter
import java.util

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.error.PebbleException
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.template.PebbleTemplate
import cz.kamenitxan.jakon.core.Settings
import cz.kamenitxan.jakon.webui.functions.PebbleExtension

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
class Pebble extends TemplateEngine {
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	val engine = new PebbleEngine.Builder()
	  .loader(loader)
	    .extension(new PebbleExtension)
	  .strictVariables(true).build()

	def render(templateName: String, path: String, context: util.Map[String, AnyRef]) {
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
	}
}