package cz.kamenitxan.jakon.logging

import cz.kamenitxan.jakon.core.task.TaskRunner

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}
import scala.collection.mutable

/**
 * Created by TPa on 15/11/2019.
 */
class InMemoryLogRepository extends LogRepository {
	private val lock = new Object
	private var logs: mutable.ArrayDeque[Log] = mutable.ArrayDeque()
	private val newLogs = new ConcurrentLinkedQueue[Log]()
	/** count of logs in logs and newLogs, ConcurrentLinkedQueue.size is O(n) */
	private val logCount = new AtomicInteger(0)
	private val scheduledExecutor = Executors.newScheduledThreadPool(1)

	scheduledExecutor.scheduleAtFixedRate(() => {
		moveLogs()
	}, 0, 10, TimeUnit.SECONDS)

	private def moveLogs(): Unit = lock.synchronized {
		var log = newLogs.poll()
		while (log != null) {
			logs.append(log)
			log = newLogs.poll()
		}
	}

	def addLog(log: Log): Unit = {
		newLogs.add(log)
		val count = logCount.incrementAndGet()
		if (LoggingSetting.getMaxLimit != 0 && count > LoggingSetting.getMaxLimit) {
			TaskRunner.runSingle(new LogCleanerTask)
		}
	}

	def clean(): Unit = lock.synchronized {
		moveLogs()
		val sizeBefore = logs.size
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
		logCount.addAndGet(logs.size - sizeBefore)
	}

	/** Returns immutable copy, safe to iterate without lock. */
	override def getLogs: Seq[Log] = lock.synchronized {
		moveLogs()
		logs.toSeq
	}
}
