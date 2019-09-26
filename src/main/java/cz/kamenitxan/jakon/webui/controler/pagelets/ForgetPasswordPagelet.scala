package cz.kamenitxan.jakon.webui.controler.pagelets

import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.mutable


/**
  * Created by TPa on 2018-11-27.
  */
@Pagelet(path = "/admin")
class ForgetPasswordPagelet extends AbstractAdminPagelet {
	override val name: String = classOf[ForgetPasswordPagelet].getName

	private val SQL_FIND_USER = "SELECT id, username, password, enabled, acl_id FROM JakonUser WHERE email = ?"

	@Get(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def get(req: Request, res: Response) = {

	}

	@Get(path = "/resetPasswordStep2", template = "pagelet/reset_password/resetPasswordStep2")
	def getStep2(req: Request, res: Response) = {

	}

	@Post(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def post(req: Request, res: Response, conn: Connection, data: ForgetPasswordData): mutable.Map[String, Any] = {
		// todo validation

		val stmt = conn.prepareStatement(SQL_FIND_USER)
		stmt.setString(1, data.email)
		val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
		if (result.entity != null) {
			val user = result.entity
			UserService.sendForgetPasswordEmail(user, req)(conn)
		}

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "PASSWORD_RESET_OK")
		redirect(req, res, "/admin")
	}

}

