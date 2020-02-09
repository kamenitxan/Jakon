package cz.kamenitxan.jakon.logging

object LogService {

	def getRepository: LogRepository = {
		LoggingSetting.getLogRepository
	}

	def getLogs:Seq[Log] = {
		LoggingSetting.getLogRepository.getLogs
	}

}
