package jakontest.logging

import cz.kamenitxan.jakon.logging._
import org.scalatest.DoNotDiscover
import jakontest.test.TestBase

@DoNotDiscover
class LoggingTest extends TestBase{

	test("log things") { _ =>
		Logger.debug("debug")
		Logger.info("info")
		Logger.warn("warn")
		Logger.error("error")
		Logger.critical("critical")
	}

	test("logDebug") { _ =>
		assert(getLogByValue("debug"))
	}

	test("logInfo") { _ =>
		assert(getLogByValue("info"))
	}

	test("logWarn") { _ =>
		assert(getLogByValue("warn"))
	}

	test("logError") { _ =>
		assert(getLogByValue("error"))
	}

	test("logCritical") { _ =>
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

	test("LogService criticalCount") { _ =>
		assert(LogService.criticalCount > 0)
	}

	test("LogService errorCount") { _ =>
		assert(LogService.errorCount > 0)
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

	test("LoggingSetting getAges") { _ =>
		assert(LoggingSetting.getMaxLimit > 0)
		assert(LoggingSetting.getSoftLimit > 0)
		assert(LoggingSetting.getMaxCriticalAge > 0)
		assert(LoggingSetting.getMaxErrorAge > 0)
		assert(LoggingSetting.getMaxWarningAge > 0)
		assert(LoggingSetting.getMaxInfoAge > 0)
		assert(LoggingSetting.getMaxDebugAge > 0)
	}

	private var slept = false
	private def getLogByValue(msg: String): Boolean = {
		if (!slept) {
			Thread.sleep(20_000)
			slept = true
		}
		val s = LogService.getLogs
		val log = s.find(_.message == msg)
		log.nonEmpty
	}

}
