package cz.kamenitxan.jakon.logging

/**
 * Created by TPa on 09.02.2020.
 */
class VoidLogRepository extends LogRepository {
	override def addLog(log: Log): Unit = {}

	override def clean(): Unit = {}

	override def getLogs: Seq[Log] = Seq.empty
}
