package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Main {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]) {
		logger.info("Jakon started")
		Director.init()
		logger.info("Jakon default init complete")
		Director.render()
	}
}