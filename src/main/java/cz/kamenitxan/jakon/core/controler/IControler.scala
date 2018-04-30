package cz.kamenitxan.jakon.core.controler

import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 01.05.16.
  */
trait IControler {
	final private val logger: Logger = LoggerFactory.getLogger(this.getClass)
	implicit val caller: IControler = this

	protected def generate(): Unit

	def generateRun(): Unit = {
		val startTime = System.currentTimeMillis()
		generate()
		val stopTime = System.currentTimeMillis()
		val elapsedTime = stopTime - startTime
		logger.info(this.getClass.getSimpleName + " generated in " + elapsedTime + " ms")
	}
}