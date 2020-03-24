package cz.kamenitxan.jakon.core.controller

import cz.kamenitxan.jakon.logging.Logger


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
  */
trait IController {
	implicit val caller: IController = this

	protected def generate(): Unit

	def generateRun(): Unit = {
		val startTime = System.currentTimeMillis()
		generate()
		val stopTime = System.currentTimeMillis()
		val elapsedTime = stopTime - startTime
		Logger.info(this.getClass.getSimpleName + " generated in " + elapsedTime + " ms")
	}
}