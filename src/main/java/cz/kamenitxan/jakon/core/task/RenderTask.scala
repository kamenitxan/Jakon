package cz.kamenitxan.jakon.core.task

import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.Director

/**
  * Created by TPa on 27.05.18.
  */

class RenderTask(period: Long, unit: TimeUnit) extends AbstractTask(period, unit) {

	override def start(): Unit = {
		Director.render()
	}
}
