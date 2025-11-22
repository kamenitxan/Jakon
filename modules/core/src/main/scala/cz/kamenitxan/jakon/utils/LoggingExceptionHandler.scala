package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.logging.Logger
import io.javalin.http.{Context, ExceptionHandler}

class LoggingExceptionHandler extends ExceptionHandler[Exception] {

	override def handle(e: Exception, ctx: Context): Unit = {
		Logger.error("", e)
	}
}
