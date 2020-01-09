package cz.kamenitxan.jakon.logging

import org.slf4j.LoggerFactory

object Logger {

	def debug(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
		log(Debug, message, cause)
	}

	def info(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
		log(Info, message, cause)
	}

	def warn(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
		log(Warning, message, cause)
	}

	def error(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
		log(Error, message, cause)
	}

	def critical(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
		log(Critical, message, cause)
	}

	private def log(severity: LogSeverity, message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName) = {
		val source = file.value + ":" + line.value
		val log = new Log(severity, message, cause, source)
		severity match {
			case Debug => LoggerFactory.getLogger(source).debug(message, cause)
			case Info => LoggerFactory.getLogger(source).info(message, cause)
			case Warning => LoggerFactory.getLogger(source).warn(message, cause)
			case Error => LoggerFactory.getLogger(source).error(message, cause)
			case Critical => LoggerFactory.getLogger(source).error(message, cause)
		}
		LogService.getRepository.addLog(log)
	}
}
