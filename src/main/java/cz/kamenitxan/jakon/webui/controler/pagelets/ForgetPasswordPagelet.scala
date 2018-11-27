package cz.kamenitxan.jakon.webui.controler.pagelets

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post, ValidationResult}
import cz.kamenitxan.jakon.utils.{PageContext, i18nUtil}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import javax.validation.Validation
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable


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

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "PASSWORD_RESET_OK")
		redirect(req, res, "/admin")
	}
}

