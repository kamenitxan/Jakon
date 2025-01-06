package cz.kamenitxan.jakon.utils.security.oauth

import java.sql.Connection
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import scala.util.Random

trait OauthProvider {

	val isEnabled: Boolean

	def authInfo(ctx: Context, redirectTo : String = null): OauthInfo

	def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean

	def logIn(ctx: Context, email: String)(implicit conn: Connection): Boolean = {
		val user = UserService.getByEmail(email)
		if (user == null) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
			false
		} else {
			Logger.info("User " + user.username + " logged in")
			ctx.sessionAttribute("user", user)
			true
		}
	}

	protected def getSecretState(ctx: Context): String = {
		val secretState = this.getClass.getSimpleName + new Random().nextInt(99999)
		secretState
	}
}

object OauthProvider {
	final val REDIRECT_TO = "redirectTo"

}
