package cz.kamenitxan.jakon.utils.security.oauth

import java.sql.Connection
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.Request

import scala.util.Random

trait OauthProvider {

	val isEnabled: Boolean

	def authInfo(req: Request, redirectTo : String = null): OauthInfo

	def handleAuthResponse(req: Request)(implicit conn: Connection): Boolean

	def logIn(req: Request, email: String)(implicit conn: Connection): Boolean = {
		val user = UserService.getByEmail(email)
		if (user == null) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
			false
		} else {
			Logger.info("User " + user.username + " logged in")
			req.session(true).attribute("user", user)
			true
		}
	}

	protected def setSecretState(req: Request): String = {
		val secretState = this.getClass.getSimpleName + new Random().nextInt(99999)
		req.session().attribute(secretState)
		secretState
	}
}

object OauthProvider {
	final val REDIRECT_TO = "redirectTo"

}
