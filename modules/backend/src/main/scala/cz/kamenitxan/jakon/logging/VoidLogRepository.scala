package cz.kamenitxan.jakon.logging

/**
 * Created by TPa on 09.02.2020.
 */
class VoidLogRepository extends LogRepository {
	override def addLog(log: Log): Unit = {
		// do nothing
	}

	override def clean(): Unit = {
		// do nothing
	}

	override def getLogs: Seq[Log] = Seq.empty
}
