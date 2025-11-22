package cz.kamenitxan.jakon.logging

import cz.kamenitxan.jakon.core.task.TaskRunner

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, TimeUnit}
import scala.collection.mutable

/**
 * Created by TPa on 15/11/2019.
 */
class InMemoryLogRepository extends LogRepository {
	var logs: mutable.ArrayDeque[Log] = mutable.ArrayDeque()
	private val newLogs: mutable.ArrayDeque[Log] = mutable.ArrayDeque()
	private val scheduledExecutor = Executors.newScheduledThreadPool(1)
	private val locked = new AtomicBoolean()

	scheduledExecutor.scheduleAtFixedRate(() => {
		moveLogs()
	}, 0, 10, TimeUnit.SECONDS)

	private def moveLogs(): Unit = {
		if (!locked.get() && locked.compareAndSet(false, true)) {
			try {
				val size = newLogs.size
				logs.appendAll(newLogs.take(size))
				newLogs.remove(0, size)
			} finally {
				locked.set(false)
			}
		}
	}

	def addLog(log: Log): Unit = {
		if (LoggingSetting.getMaxLimit != 0 && logs.size >= LoggingSetting.getMaxLimit) {
			TaskRunner.runSingle(new LogCleanerTask)
		}
		newLogs.append(log)
	}

	def clean(): Unit = {
		while (!locked.compareAndSet(false, true)) {
			Thread.sleep(1_000)
		}
		try {
			val now = LocalDateTime.now()
			val debugTime = now.minusMinutes(LoggingSetting.getMaxDebugAge)
			val infoTime = now.minusMinutes(LoggingSetting.getMaxInfoAge)
			val warningTime = now.minusMinutes(LoggingSetting.getMaxWarningAge)
			val errorTime = now.minusMinutes(LoggingSetting.getMaxErrorAge)
			val criticalTime = now.minusMinutes(LoggingSetting.getMaxCriticalAge)
			logs = logs.filter(l => if (l == null) {
				false
			} else {
				l.severity match {
					case Debug => l.time.isAfter(debugTime)
					case Info => l.time.isAfter(infoTime)
					case Warning => l.time.isAfter(warningTime)
					case Error => l.time.isAfter(errorTime)
					case Critical => l.time.isAfter(criticalTime)
				}
			})
			if (logs.size > LoggingSetting.getSoftLimit) {
				val toRemove = logs.size - LoggingSetting.getSoftLimit
				logs.remove(0, toRemove)
			}
		} finally {
			locked.set(false)
		}

	}

	override def getLogs: Seq[Log] = {
		moveLogs()
		logs.toSeq
	}
}
