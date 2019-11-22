package cz.kamenitxan.jakon.logging

import java.time.LocalDateTime

import scala.collection.mutable

/**
  * Created by TPa on 15/11/2019.
  */
class InMemoryLogRepository extends LogRepository {
	var logs: mutable.Buffer[Log] = mutable.Buffer()

	def addLog(log: Log): Unit = {
		if (LoggingSetting.getMaxLimit != 0 && logs.size >= LoggingSetting.getMaxLimit) {
			clean()
			logs.append(log)
		} else {
			logs.append(log)
		}
	}

	def clean(): Unit = {
		if (logs.size > LoggingSetting.getSoftLimit) {
			val toRemove = logs.size - LoggingSetting.getSoftLimit
			logs.remove(0, toRemove)
		}
		lazy val debugTime = LocalDateTime.now().minusMinutes(LoggingSetting.getMaxDebugAge)
		lazy val infoTime = LocalDateTime.now().minusMinutes(LoggingSetting.getMaxInfoAge)
		lazy val warningTime = LocalDateTime.now().minusMinutes(LoggingSetting.getMaxWarningAge)
		lazy val errorTime = LocalDateTime.now().minusMinutes(LoggingSetting.getMaxErrorAge)
		lazy val criticalTime = LocalDateTime.now().minusMinutes(LoggingSetting.getMaxCriticalAge)
		logs = logs.filter(l => l.severity match {
			case Debug => l.time.isAfter(debugTime)
			case Info => l.time.isAfter(infoTime)
			case Warning => l.time.isAfter(warningTime)
			case Error => l.time.isAfter(errorTime)
			case Critical => l.time.isAfter(criticalTime)
		})
	}


}