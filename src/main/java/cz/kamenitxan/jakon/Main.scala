package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.Routes
import org.slf4j.LoggerFactory

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Main {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]) {
		logger.info("Jakon started")
		Director.init()
		DBHelper.addDao(new JakonUser().getClass)
		logger.info("Jakon default init complete")
		Director.render()

		Routes.init()
	}
}