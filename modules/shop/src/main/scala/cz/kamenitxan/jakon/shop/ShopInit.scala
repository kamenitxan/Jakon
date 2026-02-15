package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.{ShopCategory, ShopProduct}

class ShopInit extends JakonInit {

	override def daoSetup(): Unit = {
		DBHelper.addDao(classOf[ShopCategory])
		DBHelper.addDao(classOf[ShopProduct])
	}

}