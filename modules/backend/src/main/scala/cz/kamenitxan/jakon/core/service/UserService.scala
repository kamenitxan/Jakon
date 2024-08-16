package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.Utils.StringImprovements
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.webui.controller.impl.Authentication
import cz.kamenitxan.jakon.webui.entity.ResetPasswordEmailEntity
import spark.Request

import java.sql.Connection
import java.util.{Calendar, Date}
import scala.util.Random

object UserService {
	implicit val cls: Class[JakonUser] = classOf[JakonUser]

	// language=SQL
	val SQL_FIND_USER_BY_USERNAME = "SELECT * FROM JakonUser WHERE username = ?"
	
	def getById(id: Int)(implicit conn: Connection): JakonUser = {
		val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE id = ?")
		stmt.setInt(1, id)
		DBHelper.selectSingleDeep(stmt)
	}

	def getByEmail(email: String)(implicit conn: Connection): JakonUser = {
		val stmt = conn.prepareStatement(Authentication.SQL_FIND_USER)
		stmt.setString(1, email)
		DBHelper.selectSingleDeep(stmt)
	}

	def getByUsername(username: String)(implicit conn: Connection): JakonUser = {
		val stmt = conn.prepareStatement(SQL_FIND_USER_BY_USERNAME)
		stmt.setString(1, username)
		DBHelper.selectSingleDeep(stmt)
	}

	def getAllUsers()(implicit conn: Connection): Seq[JakonUser] = {
		val sql = "SELECT * FROM JakonUser JOIN AclRule AR ON JakonUser.acl_id = AR.id ORDER BY AR.id;"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}

	def getMasterAdmin()(implicit conn: Connection): JakonUser = {
		val sql = "SELECT * FROM JakonUser JOIN AclRule AR ON JakonUser.acl_id = AR.id WHERE AR.masterAdmin = 1 ORDER BY AR.id LIMIT 1;"
		val stmt = conn.createStatement()
		DBHelper.selectSingleDeep(stmt, sql)
	}

	/**
	  * Sends FORGET_PASSWORD email to user
	  *
	  * @param user     jakonUser
	  * @param req      request
	  * @param expireIn expiration time in hours
	  * @param conn     DB connection
	  * @return true if successful
	  */
	def sendForgetPasswordEmail(user: JakonUser, req: Request, expireIn: Int = 1)(implicit conn: Connection): Boolean = {
		if (!Settings.isEmailEnabled) return false

		val tmpl = EmailTemplateService.getByName("FORGET_PASSWORD")

		val resetEmailEntity = new ResetPasswordEmailEntity()
		resetEmailEntity.user = user
		resetEmailEntity.secret = Random.alphanumeric.take(10).mkString
		resetEmailEntity.token = AesEncryptor.encrypt(resetEmailEntity.secret).urlEncode
		resetEmailEntity.expirationDate = {
			val cal: Calendar = Calendar.getInstance
			cal.setTime(new Date)
			cal.add(Calendar.HOUR, expireIn)
			cal.getTime
		}
		resetEmailEntity.create()

		val email = new EmailEntity("FORGET_PASSWORD", user.email, tmpl.subject, Map[String, String](
			"firstName" -> user.firstName,
			"lastName" -> user.lastName,
			"email" -> user.email,
			"username" -> user.username,
			"token" -> resetEmailEntity.token,
			"protocol" -> (if (req.raw().isSecure) "https" else "http"),
			"host" -> req.host(),
			EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry

		))
		email.create()
		true
	}
}
