package cz.kamenitxan.jakon.webui.controller.pagelets

import java.sql.Connection
import java.util.Date

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.Utils.StringImprovements
import cz.kamenitxan.jakon.webui.controller.pagelets.data.{ForgetPasswordData, SetPasswordData}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity, ResetPasswordEmailEntity}
import spark.{Request, Response}

import scala.collection.mutable


/**
  * Created by TPa on 2018-11-27.
  */
@Pagelet(path = "/admin")
class ForgetPasswordPagelet extends AbstractAdminPagelet {
	override val name: String = classOf[ForgetPasswordPagelet].getName

	private val SQL_FIND_USER = "SELECT id, username, password, enabled, acl_id, email FROM JakonUser WHERE email = ?"

	@Get(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def get(req: Request, res: Response): Unit = {
		// just render
	}

	//noinspection AccessorLikeMethodIsUnit
	@Get(path = "/resetPasswordStep2", template = "pagelet/reset_password/resetPasswordStep2")
	def getStep2(req: Request, res: Response): Unit = {
		// just render
	}

	@Post(path = "/resetPassword", template = "pagelet/reset_password/resetPassword")
	def post(req: Request, res: Response, conn: Connection, data: ForgetPasswordData): mutable.Map[String, Any] = {
		val stmt = conn.prepareStatement(SQL_FIND_USER)
		stmt.setString(1, data.email)
		val result = DBHelper.selectSingle(stmt, classOf[JakonUser])(conn)
		if (result.entity != null) {
			val user = result.entity
			UserService.sendForgetPasswordEmail(user, req)(conn)
		}

		PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "PASSWORD_RESET_OK")
		redirect(req, res, "/admin")
	}

	@Post(path = "/setPassword", template = "SetPassword")
	def postStep2(req: Request, res: Response, data: SetPasswordData): mutable.Map[String, Any] = {
		DBHelper.withDbConnection(implicit conn => {
			// language=SQL
			val sql = "SELECT * FROM ResetPasswordEmailEntity where token = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setString(1, data.token.urlEncode)
			val rpe = DBHelper.selectSingleDeep(stmt)(implicitly, classOf[ResetPasswordEmailEntity])
			if (rpe == null || rpe.expirationDate.before(new Date())) {
				PageContext.getInstance().addMessage(MessageSeverity.ERROR, "PASSWORD_CHANGE_NOT_FOUND")
				return redirect(req, res, "/")
			}
			rpe.user.password = data.password
			rpe.user.update()
			rpe.delete()
		})
		PageContext.getInstance().addMessage(MessageSeverity.SUCCESS, "PASSWORD_CHANGED")
		redirect(req, res, "/")
	}

}

