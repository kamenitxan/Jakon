package cz.kamenitxan.jakon.webui.controler.pagelets

import java.util.{Calendar, Date}

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity, ResetPasswordEmailEntity}
import javax.validation.Validation
import org.hibernate.criterion.Restrictions
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Random


/**
  * Created by TPa on 2018-11-27.
  */
@Pagelet(path = "/admin")
class ForgetPasswordPagelet extends AbstractAdminPagelet {

	@Get(path = "/resetPassword", template = "resetPassword")
	def get(req: Request, res: Response) = {

	}

	@Post(path = "/resetPassword", template = "resetPassword")
	def post(req: Request, res: Response, data: ForgetPasswordData): mutable.Map[String, Any] = {
		val factory = Validation.buildDefaultValidatorFactory
		val validator = factory.getValidator
		val violations = validator.validate(data).asScala
		val result = violations.map(v => {
			this.getClass.getSimpleName + "_" + v.getPropertyPath.toString + "_" + v.getConstraintDescriptor.getAnnotation.annotationType().getSimpleName
		})
		if (result.nonEmpty) {
			result.foreach(r => PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, r))
			return null
		}
		val ses = DBHelper.getSession
		ses.beginTransaction()
		val criteria = getSession.createCriteria(classOf[JakonUser])
		val user = criteria.add(Restrictions.eq("email", data.email)).uniqueResult().asInstanceOf[JakonUser]
		ses.getTransaction.commit()
		if (user != null) {
			sendForgetPasswordEmail(user)
		}

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "PASSWORD_RESET_OK")
		redirect(req, res, "/admin")
	}

	def sendForgetPasswordEmail(user: JakonUser): Unit = {
		if (!Settings.isEmailEnabled) return

		val session = DBHelper.getSession
		session.beginTransaction()
		val criteria = getSession.createCriteria(classOf[EmailTemplateEntity])
		val tmpl = criteria.add(Restrictions.eq("name", "FORGET_PASSWORD")).uniqueResult().asInstanceOf[EmailTemplateEntity]

		session.getTransaction.commit()
		session.close()

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

		val email = new EmailEntity("FORGET_PASSWORD", user.email, tmpl.subject, Map[String, AnyRef](
			"username" -> user.username,
			"token" -> resetEmailEntity.token,
			EmailSendTask.TMPL_LANG -> Settings.getDefaultLocale.getCountry

		))
		email.create()

	}
}

