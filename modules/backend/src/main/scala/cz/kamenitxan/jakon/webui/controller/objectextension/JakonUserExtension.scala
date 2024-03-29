package cz.kamenitxan.jakon.webui.controller.objectextension

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.mutable

/**
 * Extenion of object in administration. Extension template should be in templates/admin/objects/extension/NameOfExtensionClass.peb
 * */
@ObjectExtension(value = classOf[JakonUser], extensionType = ExtensionType.SINGLE)
@Pagelet
class JakonUserExtension extends AbstractObjectExtension {

	override def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		if (Settings.isEmailEnabled) {
			super.render(context, templatePath, req)
		} else {
			""
		}
	}

	@Get(path = "/admin/object/JakonUser/:id/resetPassword", template = "")
	def get(req: Request, res: Response): Unit = {
		val objectId = req.params(":id").toInt

		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE id = ?")
			stmt.setInt(1, objectId)
			val user = DBHelper.selectSingleDeep(stmt)(implicitly, classOf[JakonUser])
			val result = UserService.sendForgetPasswordEmail(user, req, 168)
			if (result) {
				PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "ADMIN_PASSWORD_RESET_OK")
			} else {
				PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "ADMIN_PASSWORD_RESET_FAILED")
			}
		})


		val redirectTo = if (req.headers("Referer") != null) {
			req.headers("Referer")
		} else {
			"/admin/object/JakonUser"
		}
		redirect(req, res, redirectTo)


	}

}
