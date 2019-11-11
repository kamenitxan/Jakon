package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, IPagelet}
import cz.kamenitxan.jakon.utils.PageContext
import spark.{ModelAndView, Request, Response, TemplateEngine}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by TPa on 2018-11-27.
  */
abstract class AbstractAdminPagelet extends IPagelet {
	val engine: TemplateEngine = Settings.getAdminEngine

	val name: String
	val icon: String = "fa-cog"

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
		req.session().attribute(AbstractPagelet.REQUEST_PARAMS, requestParams)
		res.redirect(target)
		null
	}
}
