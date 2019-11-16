package cz.kamenitxan.jakon.logging

/**
  * Created by TPa on 15/11/2019.
  */
sealed trait LogSeverity {

	class Debug extends LogSeverity

	class Info extends LogSeverity

	class Warning extends LogSeverity

	class Error extends LogSeverity

	class Critical extends LogSeverity

}
