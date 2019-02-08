package cz.kamenitxan.jakon.webui.controler.pagelets

import java.util.{Calendar, Date}

import cz.kamenitxan.jakon.core.Director.SELECT_EMAIL_TMPL_SQL
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity, ResetPasswordEmailEntity}
import javax.validation.Validation
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random


/**
  * Created by TPa on 2018-11-27.
  */
@Pagelet(path = "/admin")
class ForgetPasswordPagelet extends AbstractAdminPagelet {
	private val SQL_FIND_USER = "SELECT id, username, password, enabled, acl_id FROM JakonUser WHERE email = ?"

	@Get(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def get(req: Request, res: Response) = {

	}

	@Get(path = "/resetPasswordStep2", template = "pagelet/reset_password/resetPasswordStep2")
	def getStep2(req: Request, res: Response) = {

	}

	@Post(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def post(req: Request, res: Response, data: ForgetPasswordData): mutable.Map[String, Any] = {
		val factory = Validation.buildDefaultValidatorFactory
		val validator = factory.getValidator
		val violations = validator.validate(data).asScala
		val validationResult = violations.map(v => {
			this.getClass.getSimpleName + "_" + v.getPropertyPath.toString + "_" + v.getConstraintDescriptor.getAnnotation.annotationType().getSimpleName
		})
		if (validationResult.nonEmpty) {
			validationResult.foreach(r => PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, r))
			return null
		}

		val conn = DBHelper.getConnection
		val stmt = conn.prepareStatement(SQL_FIND_USER)
		stmt.setString(1, data.email)
		val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
		conn.close()
		if (result.entity != null) {
			val user = result.entity.asInstanceOf[JakonUser]
			sendForgetPasswordEmail(user, req)
		}

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "PASSWORD_RESET_OK")
		redirect(req, res, "/admin")
	}

	private def sendForgetPasswordEmail(user: JakonUser, req: Request): Unit = {
		if (!Settings.isEmailEnabled) return

		val conn = DBHelper.getConnection
		val stmt = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
		stmt.setString(1, "FORGET_PASSWORD")
		val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity.asInstanceOf[EmailTemplateEntity]
		conn.close()

		val resetEmailEntity = new ResetPasswordEmailEntity()
		resetEmailEntity.user = user
		resetEmailEntity.secret = Random.alphanumeric.take(10).mkString
		resetEmailEntity.token = AesEncryptor.encrypt(resetEmailEntity.secret)
		resetEmailEntity.expirationDate = {
			val cal: Calendar = Calendar.getInstance
			cal.setTime(new Date)
			cal.add(Calendar.HOUR, 1)
			cal.getTime
		}
		resetEmailEntity.create()

		val email = new EmailEntity("FORGET_PASSWORD", user.email, tmpl.subject, Map[String, String](
			"username" -> user.username,
			"token" -> resetEmailEntity.token,
			"protocol" -> (if (req.raw().isSecure) "https" else "http"),
			"host" -> req.host(),
			EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry

		))
		email.create()

	}
}

