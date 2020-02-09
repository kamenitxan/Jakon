package cz.kamenitxan.jakon.logging

/**
  * Created by TPa on 15/11/2019.
  */
trait LogRepository {
	def addLog(log: Log): Unit

	def getLogs: Seq[Log]

	def clean(): Unit
}
