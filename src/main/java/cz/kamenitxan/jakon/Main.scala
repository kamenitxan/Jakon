package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.logging.Logger

object Main {

	def main(args: Array[String]) {
		Logger.info("Starting Jakon")
		val app = new JakonApp()
		app.run(args)
	}

	class JakonApp() extends JakonInit {

		override def daoSetup(): Unit = {
			DBHelper.addDao(classOf[Category])
			DBHelper.addDao(classOf[Post])
			DBHelper.addDao(classOf[Page])
		}

		Director.registerControler(new PageControler)

	}
}
