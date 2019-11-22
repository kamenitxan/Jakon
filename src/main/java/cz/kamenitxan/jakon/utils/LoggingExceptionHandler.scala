package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.logging.Logger
import spark.debug.DebugScreen
import spark.{ExceptionHandler, Request, Response}

class LoggingExceptionHandler extends ExceptionHandler[Exception] {
	private val debugScreen = new DebugScreen()

	override def handle(e: Exception, request: Request, response: Response): Unit = {
		Logger.error("", e)
		debugScreen.handle(e, request, response)
	}
}
