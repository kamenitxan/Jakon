package cz.kamenitxan.jakon.utils

import org.slf4j.{Logger, LoggerFactory}
import spark.{ExceptionHandler, Request, Response}

class LoggingExceptionHandler extends ExceptionHandler[Exception] {
	private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	override def handle(e: Exception, request: Request, response: Response): Unit = {
		logger.error("", e)
	}
}
