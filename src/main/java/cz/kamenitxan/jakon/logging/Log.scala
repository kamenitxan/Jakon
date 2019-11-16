package cz.kamenitxan.jakon.logging

/**
  * Created by TPa on 15/11/2019.
  */
class Log(
           severity: LogSeverity,
           message: String,
           cause: Throwable,
           source: Class[_]
         ) {
}
