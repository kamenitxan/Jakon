package cz.kamenitxan.jakon.core.dynamic

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
import cz.kamenitxan.jakon.utils.PageContext
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.FileLoader
import spark.*
import spark.template.pebble.PebbleTemplateEngine

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

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
	val engine: TemplateEngine = new PebbleTemplateEngine(builder.build())



	def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		val ctx = if (context != null) context else mutable.Map[String, Any]()

		ctx += "jakon_messages" -> PageContext.getInstance().messages.asJava
		val rp:java.util.Map[String, Any] = req.queryMap().toMap.asInstanceOf[java.util.Map[String, Any]]
		val srp = req.session().attribute(AbstractPagelet.REQUEST_PARAMS).asInstanceOf[java.util.Map[String, Any]]
		if (srp != null) {
			rp.putAll(srp)
		}

		ctx += AbstractPagelet.REQUEST_PARAMS -> rp
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
