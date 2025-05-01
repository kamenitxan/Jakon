package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.security.oauth.{Facebook, Google}
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.ModelAndView
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context
import org.mindrot.jbcrypt.BCrypt

import java.sql.Connection
import scala.language.postfixOps

/**
  * Created by TPa on 03.09.16.
  */
object Authentication {


	// language=SQL
	val SQL_FIND_ACL = "SELECT * FROM AclRule WHERE id = ?"

	def loginGet(ctx: Context): ModelAndView = {
		val oauthProviders = {
			Google :: Facebook :: Nil
		}.filter(p => p.isEnabled).map(p => p.authInfo(ctx))

		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
			"oauthProviders" -> oauthProviders
		), "login")
	}

	def loginPost(ctx: Context): ModelAndView = {
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
					return new cz.kamenitxan.jakon.webui.Context(null, "login")
				}

				if (!user.enabled) {
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "USER_NOT_ENABLED")
					Logger.debug("User " + user.username + " is not enabled")
				} else if (checkPassword(password, user.password)) {
					if (Settings.getDeployMode == DeployMode.PRODUCTION && user.acl.masterAdmin && password == "admin") {
						PageContext.getInstance().addMessage(MessageSeverity.WARNING, "DEFAULT_ADMIN_PASSWORD")
						ctx.sessionAttribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
					}

					Logger.info("User " + user.username + " logged in")
					ctx.sessionAttribute("user", user)

					if (user.acl.adminAllowed) {
						if (Utils.isEmpty(redirectTo)) {
							ctx.redirect("/admin/index")
						} else {
							ctx.redirect(redirectTo)
						}
					} else {
						PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_NOT_ALLOWED")
					}

				} else {
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
					Logger.info("User " + user.username + " failed to provide correct password")
				}
			} finally {
				conn.close()
			}
		}
		new cz.kamenitxan.jakon.webui.Context(null, "login")
	}

	def logoutPost(ctx: Context): ModelAndView = {
		// pri vytvareni contextu se vytahuje uzivatel ze session, takze to musi byt pred invalidaci
		val jakonCtx = new cz.kamenitxan.jakon.webui.Context(null, "login")
		ctx.req().getSession.invalidate()
		ctx.redirect("/admin")
		jakonCtx
	}


	def confirmEmailGet(ctx: Context): ModelAndView = {
		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_EMAIL_CONFIRMED")
		new cz.kamenitxan.jakon.webui.Context(null, "login")
	}

	/**
	 * Hash a password using the OpenBSD bcrypt scheme
	 * @param passwordPlaintext the password to hash
	 * @return the hashed password
	 */
	def hashPassword(passwordPlaintext: String): String = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(passwordPlaintext, salt)
	}

	/**
	 * Check that a plaintext password matches a previously hashed one
	 * @param passwordPlaintext the plaintext password to verify
	 * @param storedHash the previously-hashed password
	 * @return true if the passwords match, false otherwise
	 */
	def checkPassword(passwordPlaintext: String, storedHash: String): Boolean = {
		if (null == storedHash || !storedHash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(passwordPlaintext, storedHash)
	}
}
