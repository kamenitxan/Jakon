package logging

import cz.kamenitxan.jakon.logging.{Log, Logger}
import org.scalatest.FunSuite

import scala.collection.mutable

class LoggingTest extends FunSuite {

	test("logDebug") {
		Logger.debug("debug")
		//assert(getLogByValue("debug"))
	}

	test("logInfo") {
		Logger.debug("info")
		//assert(getLogByValue("debug"))
	}

	test("logWarn") {
		Logger.debug("warn")
		//assert(getLogByValue("debug"))
	}

	test("logError") {
		Logger.debug("error")
		//assert(getLogByValue("debug"))
	}

	test("logCritical") {
		Logger.debug("critical")
		//assert(getLogByValue("debug"))
	}

	private def getLogByValue(msg: String): Boolean = {
		val s: mutable.Seq[Log] = Logger.logRepository.logs
		val log = s.find(_.message == msg)
		log.nonEmpty
	}

}
