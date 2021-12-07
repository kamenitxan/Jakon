package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.logging.Logger
import spark.{ExceptionHandler, Request, Response}

class LoggingExceptionHandler extends ExceptionHandler[Exception] {

	override def handle(e: Exception, request: Request, response: Response): Unit = {
		Logger.error("", e)
	}
}
