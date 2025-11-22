package cz.kamenitxan.jakon.logging

/**
  * Created by TPa on 15/11/2019.
  */
sealed trait LogSeverity {

}

case object Debug extends LogSeverity

case object Info extends LogSeverity

case object Warning extends LogSeverity

case object Error extends LogSeverity

case object Critical extends LogSeverity