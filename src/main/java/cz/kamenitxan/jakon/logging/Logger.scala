package cz.kamenitxan.jakon.logging

import org.slf4j.LoggerFactory

object Logger {

	def debug(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName, repositoryOnly: Boolean = false): Unit = {
		log(Debug, message, cause, repositoryOnly)
	}

	def info(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName, repositoryOnly: Boolean = false): Unit = {
		log(Info, message, cause, repositoryOnly)
	}

	def warn(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName, repositoryOnly: Boolean = false): Unit = {
		log(Warning, message, cause, repositoryOnly)
	}

	def error(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName, repositoryOnly: Boolean = false): Unit = {
		log(Error, message, cause, repositoryOnly)
	}

	def critical(message: String, cause: Throwable = null)(implicit line: sourcecode.Line, file: sourcecode.FullName, repositoryOnly: Boolean = false): Unit = {
		log(Critical, message, cause, repositoryOnly)
	}

	private def log(severity: LogSeverity, message: String, cause: Throwable = null, repositoryOnly: Boolean)(implicit line: sourcecode.Line, file: sourcecode.FullName): Unit = {
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
