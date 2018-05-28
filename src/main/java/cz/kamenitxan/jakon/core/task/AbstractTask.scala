package cz.kamenitxan.jakon.core.task

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
  * Created by TPa on 27.05.18.
  */
abstract class AbstractTask(val name: String, val period: Long, val unit: TimeUnit) extends Runnable {
	var lastRun: LocalDateTime = _
	var lastExecutionTime: Long = _

	def start(): Unit

	override def run(): Unit = {
		val startTime = System.currentTimeMillis()
		start()
		val end = System.currentTimeMillis()
		lastRun = LocalDateTime.now()
		lastExecutionTime = end - startTime
	}
}
