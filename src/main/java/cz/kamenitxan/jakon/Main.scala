package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import org.slf4j.LoggerFactory

object Main {

	private val logger = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]) {
		logger.info("Starting Jakon")
		val app = new JakonApp()
		app.run(args)
	}

	class JakonApp() extends JakonInit {

		override def daoSetup() = {
			DBHelper.addDao(classOf[Category])
			DBHelper.addDao(classOf[Post])
			DBHelper.addDao(classOf[Page])
		}

		Director.registerControler(new PageControler)

		override def adminControllers(): Unit = {
			super.adminControllers()
		}
	}
}
