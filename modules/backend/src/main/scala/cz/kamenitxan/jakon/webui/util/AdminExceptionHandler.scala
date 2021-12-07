package cz.kamenitxan.jakon.webui.util

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.logging.Logger
import spark.{ExceptionHandler, Request, Response, Spark}

import java.io.{PrintWriter, StringWriter}
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

class AdminExceptionHandler extends ExceptionHandler[Exception] {

	override def handle(e: Exception, request: Request, response: Response): Unit = {
		Logger.error("", e)
		response.status(500)


		val sw = new StringWriter
		e.printStackTrace(new PrintWriter(sw))
		val model = Map(
			"exception" -> sw.toString
		)

		try {
			response.body(Settings.getAdminEngine.render(Spark.modelAndView(model asJava, "errors/500")))
		} catch {
			case _: Exception => response.body("<html><body><h2>500 Internal Server Error</h2></body></html>")
		}

	}
}
