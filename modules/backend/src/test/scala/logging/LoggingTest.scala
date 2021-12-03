package logging

import cz.kamenitxan.jakon.logging._
import test.TestBase

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
			//TODO: proverit proc tohle loguje jen error pri testu
			val tooMany = if (i > 20) true else false
			Logger.debug(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName], repositoryOnly = tooMany)
			Logger.info(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName], repositoryOnly = tooMany)
			Logger.warn(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName], repositoryOnly = tooMany)
			Logger.error(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName], repositoryOnly = tooMany)
			Logger.critical(i.toString)(implicitly[sourcecode.Line], implicitly[sourcecode.FullName], repositoryOnly = tooMany)
		}
	}

	test("Log toString") { _ =>
		val log = new Log(Debug, "test_msg", null, "test:00")
		assert("Log(Debug, test_msg, test:00)" == log.toString)
	}

	test("VoidLogRepository addLog") { _ =>
		val rep = new VoidLogRepository
		val log = new Log(Debug, "test_msg", null, "test:00")
		try {
			rep.addLog(log)
		} catch {
			case ex: Exception => fail(ex)
		}
	}

	test("VoidLogRepository clean") { _ =>
		val rep = new VoidLogRepository
		try {
			rep.clean()
		} catch {
			case ex: Exception => fail(ex)
		}
	}

	test("VoidLogRepository getLogs") { _ =>
		val rep = new VoidLogRepository
		assert(rep.getLogs.isEmpty)
	}

	private def getLogByValue(msg: String): Boolean = {
		val s = LogService.getLogs
		val log = s.find(_.message == msg)
		log.nonEmpty
	}

}
