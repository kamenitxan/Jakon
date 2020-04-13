package cz.kamenitxan.jakon.core.dynamic

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
import cz.kamenitxan.jakon.utils.PageContext
import spark._
import spark.template.pebble.PebbleTemplateEngine

import scala.jdk.CollectionConverters._
import scala.collection.mutable

/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class AbstractPagelet extends IPagelet {
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
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
	private val builded = builder.build()
	val engine: TemplateEngine = new PebbleTemplateEngine(builded)



	def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		var ctx: mutable.Map[String, Any] = mutable.Map[String, Any]()
		if (context != null) {
			ctx = context
		}
		ctx += "jakon_messages" -> PageContext.getInstance().messages.asJava
		ctx += AbstractPagelet.REQUEST_PARAMS -> req.session().attribute(AbstractPagelet.REQUEST_PARAMS)
		engine.render(new ModelAndView(ctx.asJava, templatePath))
	}

	def redirect(req: Request, res: Response, target: String): mutable.Map[String, Any] = {
		redirect(req, res, target, null)
	}

	def redirect(req: Request, res: Response, target: String, requestParams: Map[String, Any]): mutable.Map[String, Any] = {
		req.session().attribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		req.session().attribute(AbstractPagelet.REQUEST_PARAMS, requestParams.asJava)
		res.redirect(target)
		null
	}
}

object AbstractPagelet {
	val REQUEST_PARAMS = "_RP"
}
