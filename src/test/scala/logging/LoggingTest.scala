package logging

import cz.kamenitxan.jakon.logging.{Log, LogService, Logger}
import test.TestBase

import scala.collection.mutable

class LoggingTest extends TestBase {

	test("logDebug") { _ =>
		Logger.debug("debug")
		assert(getLogByValue("debug"))
	}

	test("logInfo") { _ =>
		Logger.info("info")
		assert(getLogByValue("info"))
	}

	test("logWarn") { _ =>
		Logger.warn("warn")
		assert(getLogByValue("warn"))
	}

	test("logError") { _ =>
		Logger.error("error")
		assert(getLogByValue("error"))
	}

	test("logCritical") { _ =>
		Logger.critical("critical")
		assert(getLogByValue("critical"))
	}

	test("too many logs") { _ =>
		for (i <- 1 to 100000) {
			implicit val repositoryOnly: Boolean = true
			Logger.debug(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName])
			Logger.info(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName])
			Logger.warn(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName])
			Logger.error(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName])
			Logger.critical(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName])
		}
	}

	private def getLogByValue(msg: String): Boolean = {
		val s: mutable.Seq[Log] = LogService.getLogs
		val log = s.find(_.message == msg)
		log.nonEmpty
	}

}
