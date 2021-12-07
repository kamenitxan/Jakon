package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.mutable

@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.SINGLE)
@Pagelet
class JakonUserLogAsExtension extends AbstractObjectExtension {

	override def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		if (PageContext.getInstance().getLoggedUser.exists(_.acl.masterAdmin)) {
			super.render(context, templatePath, req)
		} else {
			""
		}
	}

	@Get(path = "/admin/object/JakonUser/:id/forceLogin", template = "")
	def get(req: Request, res: Response): Unit = {
		val objectId = req.params(":id").toInt
		if (PageContext.getInstance().getLoggedUser.exists(_.acl.masterAdmin)) {
			DBHelper.withDbConnection(implicit conn => {
				val user = UserService.getById(objectId)
				req.session(true).attribute("user", user)

				val params = Seq(user.username)
				PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "ADMIN_FORCE_LOGIN_OK", params)
			})


		} else {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_FORCE_LOGIN_FAILED")
		}

		redirect(req, res, "/admin/index")
	}

}
