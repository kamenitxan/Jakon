package cz.kamenitxan.jakon.utils.security.oauth

import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.controler.impl.Authentication.{SQL_FIND_ACL, SQL_FIND_USER}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.Request

import scala.util.Random

trait OauthProvider {

	val isEnabled: Boolean

	def authInfo(req: Request, redirectTo : String = null): OauthInfo

	def handleAuthResponse(req: Request)(implicit conn: Connection): Boolean

	def logIn(req: Request, email: String)(implicit conn: Connection): Boolean = {
		val stmt = conn.prepareStatement(SQL_FIND_USER)
		stmt.setString(1, email)

		val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
		if (result.entity == null) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
			false
		} else {
			val user = result.entity
			val stmt = conn.prepareStatement(SQL_FIND_ACL)
			stmt.setInt(1, result.foreignIds.getOrElse("acl_id", null).id)
			val aclResult = DBHelper.selectSingle(stmt, classOf[AclRule])
			user.acl = aclResult.entity

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
