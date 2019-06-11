package cz.kamenitxan.jakon.utils

import org.slf4j.{Logger, LoggerFactory}
import spark.debug.DebugScreen
import spark.{ExceptionHandler, Request, Response}

class LoggingExceptionHandler extends ExceptionHandler[Exception] {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	private val debugScreen = new DebugScreen()

	override def handle(e: Exception, request: Request, response: Response): Unit = {
		logger.error("", e)
		debugScreen.handle(e, request, response)
	}
}
