package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import scala.collection.mutable

@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.SINGLE)
@Pagelet
class JakonUserLogAsExtension extends AbstractObjectExtension {

	override def render(context: mutable.Map[String, Any], templatePath: String, ctx: Context): String = {
		if (PageContext.getInstance().getLoggedUser.exists(_.acl.masterAdmin)) {
			super.render(context, templatePath, ctx)
		} else {
			""
		}
	}

	@Get(path = "/admin/object/JakonUser/:id/forceLogin", template = "")
	def get(ctx: Context): Unit = {
		val objectId = ctx.pathParam(":id").toInt
		if (PageContext.getInstance().getLoggedUser.exists(_.acl.masterAdmin)) {
			DBHelper.withDbConnection(implicit conn => {
				val user = UserService.getById(objectId)
				ctx.sessionAttribute("user", user)

				val params = Seq(user.username)
				PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "ADMIN_FORCE_LOGIN_OK", params)
			})


		} else {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_FORCE_LOGIN_FAILED")
		}

		redirect(ctx, "/admin/index")
	}

}
