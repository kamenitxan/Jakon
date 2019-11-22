package cz.kamenitxan.jakon.logging

import scala.collection.mutable

object LogService {
	val repository = new InMemoryLogRepository

	def getRepository: LogRepository = {
		repository
	}

	def getLogs: mutable.Buffer[Log] = {
		repository.logs
	}

}
