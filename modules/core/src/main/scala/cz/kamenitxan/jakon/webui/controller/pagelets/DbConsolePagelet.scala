package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet, Post}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context

import scala.collection.mutable

/**
 * Created by TPa on 13.02.2022.
 */
@Pagelet(path = "/admin/dbconsole", showInAdmin = true)
class DbConsolePagelet extends AbstractAdminPagelet {
	override val name: String = this.getClass.getSimpleName

	@Get(path = "",template = "pagelet/db_console/db_console")
	def registrationGet(): Unit = {
		// just render
	}

	@Post(path = "", template = "")
	def registrationPost(ctx: Context, data: DbConsoleData): mutable.Map[String, Any] = {
		try {
			DBHelper.withDbConnection(implicit conn => {
				val stmt = conn.createStatement()
				stmt.execute(data.sql)
				PageContext.getInstance().messages += new Message(MessageSeverity.SUCCESS, "SQL_EXECUTED")
			})
		} catch {
			case ex: Exception =>
				Logger.error("Query execution failed", ex)
				PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "SQL_EXECUTION_FAILED", Seq(ex.getMessage))
		}

		redirect(ctx, "/admin/dbconsole")
		null
	}
}

class DbConsoleData {
	var sql: String = _
}
