package cz.kamenitxan.jakon.logging

import scala.collection.mutable

/**
  * Created by TPa on 15/11/2019.
  */
object InMemoryLogRepository {
	private val logs: mutable.Seq[Log] = mutable.Seq()

	def addLog(log: Log): Unit = {

	}
}
