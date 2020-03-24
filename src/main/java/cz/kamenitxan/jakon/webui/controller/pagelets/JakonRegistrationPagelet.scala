package cz.kamenitxan.jakon.webui.controller.pagelets

import java.util.{Calendar, Date}

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.webui.controler.pagelets.data.JakonRegistrationData
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.mutable
import scala.util.Random

@Pagelet(path = "/admin/register")
class JakonRegistrationPagelet extends AbstractAdminPagelet {
	override val name: String = this.getClass.getSimpleName
	private val SQL_SELECT_EMAIL_TMPL = "SELECT subject FROM EmailTemplateEntity WHERE name = \"REGISTRATION\""

	@Get(path = "",template = "pagelet/registration/register")
	def registrationGet(response: Response) = {
		// just render
	}


	@Post(path = "", template = "")
	def registrationPost(req: Request, res: Response, data: JakonRegistrationData): mutable.Map[String, Any] = {
		val email = req.queryParams("email")
		val password = req.queryParams("password")
		val password2 = req.queryParams("password2")
		val firstName = req.queryParams("firstname")
		val lastName = req.queryParams("lastname")
		if (!password.equals(password2)) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "REGISTRATION_PASSWORD_NOT_SAME")
			return null
		}
		val user = new JakonUser()
		user.email = email
		user.username = email
		user.password = password
		user.firstName = firstName
		user.lastName = lastName
		user.create()

		sendRegistrationEmail(user)

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "REGISTRATION_SUCCESSFUL")
		redirect(req, res, "/admin")
	}

	def sendRegistrationEmail(user: JakonUser): Unit = {
		if (!Settings.isEmailEnabled) return

		val conn = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()
			val tmpl = DBHelper.selectSingle(stmt, SQL_SELECT_EMAIL_TMPL, classOf[EmailTemplateEntity]).entity

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

}
