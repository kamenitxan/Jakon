package cz.kamenitxan.jakon.core.task

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}

import org.slf4j.LoggerFactory

import scala.collection.mutable


/**
  * Created by TPa on 27.05.18.
  */
object TaskRunner {
	private val logger = LoggerFactory.getLogger(this.getClass)
	private val scheduledExecutor = Executors.newScheduledThreadPool(1)
	var taskList: mutable.MutableList[AbstractTask] = mutable.MutableList[AbstractTask]()

	def schedule(task: AbstractTask): ScheduledFuture[_] = {
		scheduledExecutor.scheduleAtFixedRate(task, 0, task.period, task.unit)
	}

	def runSingle(task: AbstractTask): ScheduledFuture[_] = {
		scheduledExecutor.schedule(task, 0, TimeUnit.SECONDS)
	}

	def registerTask(task: AbstractTask): Unit = {
		taskList += task
		logger.info("Task " + task.name + " was registered")
	}

	def startTaskRunner(): Unit = {
		taskList.foreach(task => if (task.period > 0) schedule(task))
	}
}
