package cz.kamenitxan.jakon.logging

object LogService {

	def getRepository: LogRepository = {
		LoggingSetting.getLogRepository
	}

	def getLogs: Seq[Log] = {
		LoggingSetting.getLogRepository.getLogs
	}

	def criticalCount: Int = {
		getLogs.count(_.severity == Critical)
	}

	def errorCount: Int = {
		getLogs.count(_.severity == Error)
	}

}
