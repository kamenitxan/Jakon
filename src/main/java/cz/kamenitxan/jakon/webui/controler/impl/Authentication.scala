package cz.kamenitxan.jakon.webui.controler.impl

import java.util.{Calendar, Date}

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.utils.security.oauth.{Facebook, Google}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, Message, MessageSeverity}
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.{Logger, LoggerFactory}
import spark.{ModelAndView, Request, Response}

import scala.language.postfixOps
import scala.util.Random

/**
  * Created by TPa on 03.09.16.
  */
object Authentication {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val SQL_FIND_USER = "SELECT * FROM JakonUser WHERE email = ?"
	val SQL_FIND_ACL = "SELECT id, name, adminAllowed, masterAdmin FROM AclRule WHERE id = ?"
	private val SQL_SELECT_EMAIL_TMPL = "SELECT subject FROM EmailTemplateEntity WHERE name = \"REGISTRATION\""

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
		if (email != null && password != null) {
			val conn = DBHelper.getConnection
			try {
				val stmt = conn.prepareStatement(SQL_FIND_USER)
				stmt.setString(1, email)

				val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
				if (result.entity == null) {
					logger.info("User " + email + " not fould when loggin")
					PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
					return new Context(null, "login")
				}

				val user = result.entity.asInstanceOf[JakonUser]
				if (checkPassword(password, user.password) && user.enabled) {
					val stmt = conn.prepareStatement(SQL_FIND_ACL)
					stmt.setInt(1, result.foreignIds.getOrElse("acl_id", null).id)
					val aclResult = DBHelper.selectSingle(stmt, classOf[AclRule])
					user.acl = aclResult.entity.asInstanceOf[AclRule]

					logger.info("User " + user.username + " logged in")
					req.session(true).attribute("user", user)
					res.redirect("/admin/index")
				} else {
					logger.info("User " + user.username + " failed to provide correct password")
				}
			} finally {
				conn.close()
			}
		}
		new Context(null, "login")
	}

	def logoutPost(req: Request, res: Response): ModelAndView = {
		req.session().invalidate()
		res.redirect("/admin")
		new Context(null, "login")
	}

	def registrationGet(response: Response): ModelAndView = {
		new Context(null, "register")
	}

	def registrationPost(req: Request, res: Response): ModelAndView = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		val password2 = req.queryParams("password2")
		val firstname = req.queryParams("firstname")
		val lastname = req.queryParams("lastname")
		if (!password.equals(password2)) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "REGISTRATION_PASSWORD_NOT_SAME")
			return new Context(null, "register")
		}
		val user = new JakonUser()
		user.email = email
		user.username = email
		user.password = password
		user.firstName = firstname
		user.lastName = lastname
		user.create()

		sendRegistrationEmail(user)

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_SUCCESSFUL")
		new Context(null, "login")
	}

	def confirmEmailGet(req: Request, res: Response): ModelAndView = {
		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_EMAIL_CONFIRMED")
		new Context(null, "login")
	}

	def sendRegistrationEmail(user: JakonUser): Unit = {
		if (!Settings.isEmailEnabled) return

		val conn = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()
			val tmpl = DBHelper.selectSingle(stmt, SQL_SELECT_EMAIL_TMPL, classOf[EmailTemplateEntity]).entity.asInstanceOf[EmailTemplateEntity]

			val confirmEmailEntity = new ConfirmEmailEntity()
			confirmEmailEntity.user = user
			confirmEmailEntity.secret = Random.alphanumeric.take(10).mkString
			confirmEmailEntity.token = AesEncryptor.encrypt(confirmEmailEntity.secret)
			confirmEmailEntity.expirationDate = {
				val cal: Calendar = Calendar.getInstance
				cal.setTime(new Date)
				cal.add(Calendar.DATE, 2)
				cal.getTime
			}
			confirmEmailEntity.create()

			val email = new EmailEntity("REGISTRATION", user.email, tmpl.subject, Map[String, String](
				"username" -> user.username,
				"token" -> confirmEmailEntity.token,
				EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry

			))
			email.create()
		} finally {
			conn.close()
		}
	}


	def hashPassword(password_plaintext: String): String = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(password_plaintext, salt)
	}

	def checkPassword(password_plaintext: String, stored_hash: String): Boolean = {
		if (null == stored_hash || !stored_hash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(password_plaintext, stored_hash)
	}
}
