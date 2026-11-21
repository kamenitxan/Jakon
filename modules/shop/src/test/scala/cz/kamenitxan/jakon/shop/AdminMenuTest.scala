package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.shop.entity.{Cart, ShopCategory, ShopProduct}
import cz.kamenitxan.jakon.shop.it.{TestShopCategory, TestShopProduct}
import cz.kamenitxan.jakon.webui.Context
import org.scalatest.funsuite.AnyFunSuite

class AdminMenuTest extends AnyFunSuite {

	test("shop objects without a registered subclass are shown in the menu") {
		val classes: Seq[Class[? <: JakonObject]] = Seq(classOf[ShopCategory], classOf[ShopProduct], classOf[Cart])
		assert(Context.menuClasses(classes) == classes)
	}

	test("shop object extended by a registered subclass is hidden from the menu") {
		val classes: Seq[Class[? <: JakonObject]] = Seq(
			classOf[ShopCategory], classOf[ShopProduct], classOf[Cart], classOf[TestShopProduct], classOf[TestShopCategory]
		)
		assert(Context.menuClasses(classes) == Seq(classOf[Cart], classOf[TestShopProduct], classOf[TestShopCategory]))
	}
}
