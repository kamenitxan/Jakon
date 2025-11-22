package cz.kamenitxan.jakon.core.task

import cz.kamenitxan.jakon.core.Director

import java.util.concurrent.TimeUnit

/**
  * Created by TPa on 27.05.18.
  */

class RenderTask(period: Long, unit: TimeUnit) extends AbstractTask(period, unit) {

	override def start(): Unit = {
		Director.render()
	}
}
