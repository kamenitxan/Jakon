package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controller.PageController
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.Category

class ShopInit() extends JakonInit {

	override def daoSetup(): Unit = {
		DBHelper.addDao(classOf[Category])
	}

	Director.registerController(new PageController)

}