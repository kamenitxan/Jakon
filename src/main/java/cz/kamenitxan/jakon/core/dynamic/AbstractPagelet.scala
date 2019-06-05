package cz.kamenitxan.jakon.core.dynamic

import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.utils.PageContext
import spark._
import spark.template.pebble.PebbleTemplateEngine

import scala.collection.JavaConverters._
import scala.collection.mutable


/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class AbstractPagelet {
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	val engine: TemplateEngine = new PebbleTemplateEngine(loader)


	def render(context: mutable.Map[String, Any], templatePath: String): String = {
		var ctx: mutable.Map[String, Any] = mutable.Map[String, Any]()
		if (context != null) {
			ctx = context
		}
		ctx += "jakon_messages" -> PageContext.getInstance().messages.asJava
		engine.render(new ModelAndView(ctx.asJava, templatePath))
	}

	def redirect(req: Request, res: Response, target: String): mutable.Map[String, Any] = {
		req.session().attribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		res.redirect(target)
		null
	}
}