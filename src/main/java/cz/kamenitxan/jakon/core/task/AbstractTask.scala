package cz.kamenitxan.jakon.core.task

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.KeyValueService
import cz.kamenitxan.jakon.logging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by TPa on 27.05.18.
  */
abstract class AbstractTask(val period: Long, val unit: TimeUnit)(implicit val name: sourcecode.Name) extends Runnable {
	private val logger = LoggerFactory.getLogger(this.getClass)
	var lastRun: LocalDateTime = _
	var lastExecutionTime: Long = _
	var lastRunSuccessful: Boolean = false

	def paused: Boolean = {
		val taskName = this.name.value
		DBHelper.withDbConnection(implicit conn => {
			KeyValueService.getByKey(taskName + "_disabled").nonEmpty
		})
	}

	def nextRun: LocalDateTime = {
		if (lastRun != null && !paused) {
			lastRun.plusSeconds(TimeUnit.SECONDS.convert(period, unit))
		} else {
			null
		}
	}


	def start(): Unit

	override def run(): Unit = {
		Logger.debug(s"Task ${this.name.value} started")
		val startTime = System.currentTimeMillis()
		try {
			start()
			lastRunSuccessful = true
		} catch {
			case ex: Exception =>
				lastRunSuccessful = false
				logger.error("Error while running task", ex)
		}

		val end = System.currentTimeMillis()
		lastRun = LocalDateTime.now()
		lastExecutionTime = end - startTime
		Logger.debug(s"Task ${this.name.value} finished")
	}
}
