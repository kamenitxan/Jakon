package cz.kamenitxan.jakon.logging

import java.time.LocalDateTime

/**
  * Created by TPa on 15/11/2019.
  */
class Log(
           val severity: LogSeverity,
           val message: String,
           val cause: Throwable,
           val source: String
         ) {

	val time: LocalDateTime = LocalDateTime.now()

	override def toString: String = s"Log($severity, $message, $source)"
}
