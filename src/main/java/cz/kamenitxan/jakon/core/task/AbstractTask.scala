package cz.kamenitxan.jakon.core.task

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import org.slf4j.LoggerFactory

/**
  * Created by TPa on 27.05.18.
  */
abstract class AbstractTask(val name: String, val period: Long, val unit: TimeUnit) extends Runnable {
	private val logger = LoggerFactory.getLogger(this.getClass)
	var lastRun: LocalDateTime = _
	var lastExecutionTime: Long = _
	var lastRunSuccessful: Boolean = false

	def start(): Unit

	override def run(): Unit = {
		val startTime = System.currentTimeMillis()
		try {
			start()
			lastRunSuccessful = true
		} catch {
			case ex: Exception => {
				lastRunSuccessful = false
				logger.error("Error while running task", ex)
			}
		}

		val end = System.currentTimeMillis()
		lastRun = LocalDateTime.now()
		lastExecutionTime = end - startTime
	}
}
