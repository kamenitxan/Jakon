package cz.kamenitxan.jakon.webui.util

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.logging.Logger
import io.javalin.http.{Context, ExceptionHandler}

import java.io.{PrintWriter, StringWriter}
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps

class AdminExceptionHandler extends ExceptionHandler[Exception] {

	override def handle(e: Exception, ctx: Context): Unit = {
		Logger.error("", e)
		ctx.status(500)


		val sw = new StringWriter
		e.printStackTrace(new PrintWriter(sw))
		val model = Map(
			"exception" -> sw.toString
		)

		try {
			ctx.result(Settings.getAdminEngine.render("errors/500", model asJava, ctx))
		} catch {
			case _: Exception => ctx.result("<html><body><h2>500 Internal Server Error</h2></body></html>")
		}

	}
}
