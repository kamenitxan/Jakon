package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import scala.collection.mutable

/**
 * Extenion of object in administration. Extension template should be in templates/admin/objects/extension/NameOfExtensionClass.peb
 * */
@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.SINGLE)
@Pagelet
class JakonUserExtension extends AbstractObjectExtension {

	override def render(context: mutable.Map[String, Any], templatePath: String, ctx: Context): String = {
		if (Settings.isEmailEnabled) {
			super.render(context, templatePath, ctx)
		} else {
			""
		}
	}

	@Get(path = "/admin/object/JakonUser/{id}/resetPassword", template = "")
	def get(ctx: Context): Unit = {
		val objectId = ctx.pathParam("id").toInt

		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE id = ?")
			stmt.setInt(1, objectId)
			val user = DBHelper.selectSingleDeep(stmt)(implicitly, classOf[JakonUser])
			val result = UserService.sendForgetPasswordEmail(user, ctx, 168)
			if (result) {
				PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "ADMIN_PASSWORD_RESET_OK")
			} else {
				PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_PASSWORD_RESET_FAILED")
			}
		})


		val redirectTo = if (ctx.header("Referer") != null) {
			ctx.header("Referer")
		} else {
			"/admin/object/JakonUser"
		}
		redirect(ctx, redirectTo)


	}

}
