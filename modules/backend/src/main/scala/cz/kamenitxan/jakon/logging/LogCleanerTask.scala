package cz.kamenitxan.jakon.logging

import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.task.AbstractTask

/**
  * Created by TPa on 16/11/2019.
  */
class LogCleanerTask extends AbstractTask(5, TimeUnit.MINUTES) {

	override def start(): Unit = {
		LogService.getRepository.clean()
	}
}
