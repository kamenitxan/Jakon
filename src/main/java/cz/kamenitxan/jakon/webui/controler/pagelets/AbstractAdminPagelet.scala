package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.AbstractPagelet
import cz.kamenitxan.jakon.utils.PageContext
import spark.{Request, Response, TemplateEngine}

import scala.collection.mutable

/**
  * Created by TPa on 2018-11-27.
  */
abstract class AbstractAdminPagelet {
	val engine: TemplateEngine = Settings.getAdminEngine

	val name: String
	val icon: String = "fa-cog"

	def redirect(req: Request, res: Response, target: String): mutable.Map[String, Any] = {
		redirect(req, res, target, null)
	}

	def redirect(req: Request, res: Response, target: String, requestParams: AnyRef): mutable.Map[String, Any] = {
		req.session().attribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		req.session().attribute(AbstractPagelet.REQUEST_PARAMS, requestParams)
		res.redirect(target)
		null
	}
}
