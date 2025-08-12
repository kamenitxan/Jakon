package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.security.AuthUtils
import cz.kamenitxan.jakon.utils.security.oauth.{Facebook, Google}
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Routes
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import java.sql.Connection
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
 * Created by Kamenitxan on 05.08.2025
 */
@Pagelet(path = Routes.AdminPrefix, showInAdmin = false)
class AdminAuthPagelet extends AbstractAdminPagelet {

	override val name: String = classOf[AdminAuthPagelet].getSimpleName

	@Get(path = "", template = "login")
	def loginGet(ctx: Context): mutable.Map[String, Any] = {
		val oauthProviders = {
			Google :: Facebook :: Nil
		}.filter(p => p.isEnabled).map(p => p.authInfo(ctx))

		mutable.Map[String, Any](
			"oauthProviders" -> oauthProviders.asJava
		)
	}

	@Post(path = "", template = "login")
	def loginPost(ctx: Context): mutable.Map[String, Any] = {
		val email = ctx.formParam("email")
		val password = ctx.formParam("password")
		val redirectTo = ctx.queryParam("redirect_to")
		if (email != null && password != null) {
			implicit val conn: Connection = DBHelper.getConnection
			try {
				val user = UserService.getByEmail(email)
				if (user == null) {
					Logger.info("User " + email + " not found when logging in")
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
					return mutable.Map.empty
				}

				if (!user.enabled) {
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "USER_NOT_ENABLED")
					Logger.debug("User " + user.username + " is not enabled")
				} else if (AuthUtils.isUnderLoginAttemptLimit(user.id)) {
					if (AuthUtils.checkPassword(password, user.password)) {
						if (Settings.getDeployMode == DeployMode.PRODUCTION && user.acl.masterAdmin && password == "admin") {
							PageContext.getInstance().addMessage(MessageSeverity.WARNING, "DEFAULT_ADMIN_PASSWORD")
							ctx.sessionAttribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
						}
						AuthUtils.resetLoginAttempts(user.id)

						Logger.info("User " + user.username + " logged in")
						ctx.sessionAttribute("user", user)

						if (user.acl.adminAllowed) {
							if (Utils.isEmpty(redirectTo)) {
								ctx.redirect(Routes.AdminPrefix + "/index")
							} else {
								ctx.redirect(redirectTo)
							}
						} else {
							PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_NOT_ALLOWED")
						}
					} else { // wrong password
						AuthUtils.incrementLoginAttempts(user.id)
						PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
						Logger.info("User " + user.username + " failed to provide correct password")
					}
				} else {
					// too many login attempts
					AuthUtils.incrementLoginAttempts(user.id)
					Logger.info("User " + user.username + " failed to login because of too many login attempts")
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "TOO_MANY_LOGIN_ATTEMPTS")
				}
			} finally {
				conn.close()
			}
		}
		mutable.Map.empty
	}

	@Get(path = "/logout", template = "login")
	def logout(ctx: Context): mutable.Map[String, Any] = {
		ctx.req().getSession.invalidate()
		ctx.redirect("/admin")
		mutable.Map.empty
	}


	/*def confirmEmailGet(ctx: Context): ModelAndView = {
		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_EMAIL_CONFIRMED")
		new cz.kamenitxan.jakon.webui.Context(null, "login")
	}*/

}
