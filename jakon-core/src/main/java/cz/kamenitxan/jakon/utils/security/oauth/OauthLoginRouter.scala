package cz.kamenitxan.jakon.utils.security.oauth

import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.controler.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.MessageSeverity
import org.slf4j.LoggerFactory
import spark.{Request, Response}
import cz.kamenitxan.jakon.utils.Utils._

@Pagelet(path = "/admin/login/oauth")
class OauthLoginRouter extends AbstractAdminPagelet {
	private val logger = LoggerFactory.getLogger(classOf[OauthLoginRouter])
	override val name: String = classOf[OauthLoginRouter].getName

	@Get(path = "", template = "")
	def get(req: Request, res: Response) = {
		val provider = req.queryParams("provider")
		val success = provider match {
			case p if p == Google.getClass.getSimpleName =>
				Google.handleAuthResponse(req)
			case p =>
				logger.error("Unknown oauth provider: " + p)
				false
		}
		if (success) {
			val redirectTo = req.queryParams(OauthProvider.REDIRECT_TO)
			res.redirect(redirectTo.getOrElse("/admin/index"))
		} else {
			PageContext.getInstance().addMessage(MessageSeverity.ERROR, "OAUTH_LOGIN_FAILED")
			res.redirect("/admin")
		}
	}
}
