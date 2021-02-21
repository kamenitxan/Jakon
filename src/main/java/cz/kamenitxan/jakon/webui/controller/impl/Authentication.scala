package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.security.oauth.{Facebook, Google}
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import org.mindrot.jbcrypt.BCrypt
import spark.{ModelAndView, Request, Response}

import java.sql.Connection
import scala.language.postfixOps

/**
  * Created by TPa on 03.09.16.
  */
object Authentication {

	// language=SQL
	val SQL_FIND_USER = "SELECT * FROM JakonUser WHERE email = ?"
	// language=SQL
	val SQL_FIND_ACL = "SELECT * FROM AclRule WHERE id = ?"

	def loginGet(req: Request): ModelAndView = {
		val oauthProviders = {
			Google :: Facebook :: Nil
		}.filter(p => p.isEnabled).map(p => p.authInfo(req))

		new Context(Map[String, Any](
			"oauthProviders" -> oauthProviders
		), "login")
	}

	def loginPost(req: Request, res: Response): ModelAndView = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		val redirectTo = req.queryParams("redirect_to")
		if (email != null && password != null) {
			implicit val conn: Connection = DBHelper.getConnection
			try {
				val stmt = conn.prepareStatement(SQL_FIND_USER)
				stmt.setString(1, email)

				val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
				if (result.entity == null) {
					Logger.info("User " + email + " not found when logging in")
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
					return new Context(null, "login")
				}

				val user = result.entity
				if (!user.enabled) {
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "USER_NOT_ENABLED")
					Logger.debug("User " + user.username + " is not enabled")
				} else if (checkPassword(password, user.password)) {
					val stmt = conn.prepareStatement(SQL_FIND_ACL)
					stmt.setInt(1, result.foreignIds.getOrElse("acl_id", null).ids.head)
					val aclResult = DBHelper.selectSingle(stmt, classOf[AclRule])
					user.acl = aclResult.entity

					if (Settings.getDeployMode == DeployMode.PRODUCTION && user.acl.masterAdmin && password == "admin") {
						PageContext.getInstance().addMessage(MessageSeverity.WARNING, "DEFAULT_ADMIN_PASSWORD")
						req.session().attribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
					}

					Logger.info("User " + user.username + " logged in")
					req.session(true).attribute("user", user)

					if (user.acl.adminAllowed) {
						if (Utils.isEmpty(redirectTo)) {
							res.redirect("/admin/index")
						} else {
							res.redirect(redirectTo)
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
		new Context(null, "login")
	}

	def logoutPost(req: Request, res: Response): ModelAndView = {
		// pri vytvareni contextu se vytahuje uzivatel ze session, takze to musi byt pred invalidaci
		val ctx = new Context(null, "login")
		req.session().invalidate()
		res.redirect("/admin")
		ctx
	}


	def confirmEmailGet(req: Request, res: Response): ModelAndView = {
		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_EMAIL_CONFIRMED")
		new Context(null, "login")
	}


	def hashPassword(passwordPlaintext: String): String = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(passwordPlaintext, salt)
	}

	def checkPassword(passwordPlaintext: String, storedHash: String): Boolean = {
		if (null == storedHash || !storedHash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(passwordPlaintext, storedHash)
	}
}
