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
	private val taskFutures: mutable.Map[String, ScheduledFuture[?]] = mutable.Map[String, ScheduledFuture[?]]()
	private val singleRunFutures: mutable.Map[String, ScheduledFuture[?]] = mutable.Map[String, ScheduledFuture[?]]()

	def schedule(task: AbstractTask): Unit = {
		if (!task.paused) {
			val f: ScheduledFuture[?] = scheduledExecutor.scheduleAtFixedRate(task, 0, task.period, task.unit)
			taskFutures += (task.name.value -> f)
		}
	}

	/** Can be called from many threads (e.g. log cleaning), check and schedule must be atomic. */
	def runSingle(task: AbstractTask): Unit = singleRunFutures.synchronized {
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
		taskFutures.get(task.name.value).foreach(f => {
			f.cancel(false)
			taskFutures -= task.name.value
		})
	}

	def shutdown(): Boolean = {
		scheduledExecutor.shutdown()
		scheduledExecutor.awaitTermination(30, TimeUnit.SECONDS)
	}
}
