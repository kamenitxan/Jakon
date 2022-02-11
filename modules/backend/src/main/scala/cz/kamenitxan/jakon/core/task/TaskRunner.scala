package cz.kamenitxan.jakon.core.task

import cz.kamenitxan.jakon.logging.Logger

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import scala.collection.mutable


/**
  * Created by TPa on 27.05.18.
  */
object TaskRunner {
	private val scheduledExecutor = Executors.newScheduledThreadPool(1)
	var taskList: mutable.ArrayDeque[AbstractTask] = mutable.ArrayDeque[AbstractTask]()
	private val taskFutures: mutable.Map[String, ScheduledFuture[_]] = mutable.Map[String, ScheduledFuture[_]]()
	private val singleRunFutures: mutable.Map[String, ScheduledFuture[_]] = mutable.Map[String, ScheduledFuture[_]]()

	def schedule(task: AbstractTask): Unit = {
		if (!task.paused) {
			val f: ScheduledFuture[_] = scheduledExecutor.scheduleAtFixedRate(task, 0, task.period, task.unit)
			taskFutures += (task.name.value -> f)
		}
	}

	def runSingle(task: AbstractTask): Unit = {
		val ef = singleRunFutures.get(task.name.value)
		if (ef.isEmpty || ef.get.isDone) {
			val f = scheduledExecutor.schedule(task, 0, TimeUnit.SECONDS)
			if (ef.isEmpty) {
				singleRunFutures += (task.name.value -> f)
			} else {
				singleRunFutures(task.name.value) = f
			}

		}
	}

	def registerTask(task: AbstractTask): Unit = {
		taskList += task
		Logger.info("Task " + task.name.value + " was registered")
	}

	def startTaskRunner(): Unit = {
		taskList.foreach(task => if (task.period > 0) schedule(task))
	}

	def stop(task: AbstractTask): Unit = {
		val f = taskFutures(task.name.value)
		f.cancel(false)
		taskFutures -= task.name.value
	}
}
