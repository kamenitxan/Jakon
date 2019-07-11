package jakon

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{BasicJakonObject, Category, Page, Post}

class TestJakonApp extends JakonInit {

	override def daoSetup() = {
		DBHelper.addDao(classOf[Category])
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Page])
		DBHelper.addDao(classOf[BasicJakonObject])
	}

	Director.registerControler(new PageControler)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
