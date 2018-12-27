package cz.kamenitxan.jakon.example

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import org.slf4j.impl.SimpleLogger

class JakonApp extends JakonInit{
	System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")

	DBHelper.addDao(classOf[Category])
	DBHelper.addDao(classOf[Post])
	DBHelper.addDao(classOf[Page])

	Director.registerControler(new PageControler)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
