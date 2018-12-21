package cz.kamenitxan.jakon.webui.controler.impl

import java.util.Date
import java.util.Calendar

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, Message, MessageSeverity}
import org.hibernate.criterion.Restrictions
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

	private val SQL_FIND_USER = "SELECT id, username, password, enabled, acl_id FROM JakonUser WHERE email = ?"
	private val SQL_FIND_ACL = "SELECT id, name, adminAllowed, masterAdmin FROM AclRule WHERE id = ?"

	def loginGet(response: Response): ModelAndView = {
		new Context(null, "login")
	}

	def loginPost(req: Request, res: Response): ModelAndView = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		if (email != null && password != null) {
			val stmt = DBHelper.getPreparedStatement(SQL_FIND_USER)
			stmt.setString(1, email)

			val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
			if (result.entity == null) {
				logger.info("User " + email + " not fould when loggin")
				PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "WRONG_EMAIL_OR_PASSWORD")
				return new Context(null, "login")
			}

			val user = result.entity.asInstanceOf[JakonUser]
			if (checkPassword(password, user.password) && user.enabled) {
				val stmt = DBHelper.getPreparedStatement(SQL_FIND_ACL)
				stmt.setInt(1, result.foreignIds.getOrElse("acl_id", null).id)
				val aclResult = DBHelper.selectSingle(stmt, classOf[AclRule])
				user.acl = aclResult.entity.asInstanceOf[AclRule]

				logger.info("User " + user.username + " logged in")
				req.session(true).attribute("user", user)
				res.redirect("/admin/index")
			} else {
				logger.info("User " + user.username + " failed to provide correct password")
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
		createUser(user)

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

		val session = DBHelper.getSession
		session.beginTransaction()
		val criteria = getSession.createCriteria(classOf[EmailTemplateEntity])
		val tmpl = criteria.add(Restrictions.eq("name", "REGISTRATION")).uniqueResult().asInstanceOf[EmailTemplateEntity]

		session.getTransaction.commit()
		session.close()

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

		val email = new EmailEntity("REGISTRATION", user.email, tmpl.subject, Map[String, AnyRef](
			"username" -> user.username,
			"token" -> confirmEmailEntity.token,
			EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry

		))
		email.create()

	}

	def createUser(user: JakonUser): JakonUser = {
		user.password = hashPassword(user.password)
		val session = DBHelper.getSession
		session.beginTransaction()
		val id = session.save(user)
		session.getTransaction.commit()
		session.close()
		user.setId(id.asInstanceOf[Int])
		user
	}

	def hashPassword(password_plaintext: String) = {
		val salt = BCrypt.gensalt(12)
		BCrypt.hashpw(password_plaintext, salt)
	}

	def checkPassword(password_plaintext: String, stored_hash: String) = {
		if (null == stored_hash || !stored_hash.startsWith("$2a$"))
			throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison")

		BCrypt.checkpw(password_plaintext, stored_hash)
	}
}
