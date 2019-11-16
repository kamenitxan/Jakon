package cz.kamenitxan.jakon.logging

import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.task.AbstractTask

/**
  * Created by TPa on 16/11/2019.
  */
class LogCleanerClass extends AbstractTask(classOf[LogCleanerClass].getName, 5, TimeUnit.MINUTES) {

	override def start(): Unit = {

	}
}
