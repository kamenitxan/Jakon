package cz.kamenitxan.jakon.utils.security.oauth

import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.Utils.*
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.MessageSeverity
import io.javalin.http.Context

import java.sql.Connection

@Pagelet(path = "/admin/login/oauth")
class OauthLoginRouter extends AbstractAdminPagelet {
	override val name: String = classOf[OauthLoginRouter].getName

	@Get(path = "", template = "")
	def get(ctx: Context, conn: Connection) = {
		val provider = ctx.queryParam("provider")
		val success = provider match {
			case p if p == Google.getClass.getSimpleName =>
				Google.handleAuthResponse(ctx)(conn)
			case p =>
				Logger.error("Unknown oauth provider: " + p)
				false
		}
		if (success) {
			val redirectTo = ctx.queryParam(OauthProvider.REDIRECT_TO)
			ctx.redirect(redirectTo.getOrElse("/admin/index"))
		} else {
			PageContext.getInstance().addMessage(MessageSeverity.ERROR, "OAUTH_LOGIN_FAILED")
			ctx.redirect("/admin")
		}
	}
}
