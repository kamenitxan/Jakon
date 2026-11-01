package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.ShopInit
import cz.kamenitxan.jakon.shop.service.{ShopCategoryService, ShopProductService}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal
import java.util.Locale

/** Shop services work with a subclass of ShopCategory set as the category class. */
@DoNotDiscover
class ShopCategorySubclassTest extends AnyFunSuite with ShopDbSupport {

	private def withCategoryClass[T](f: => T): T = {
		val original = ShopInit.categoryClass
		ShopInit.categoryClass = classOf[TestShopCategory]
		try f finally ShopInit.categoryClass = original
	}

	private def createSubCategory(printTimes: Map[String, String]): TestShopCategory = {
		val c = new TestShopCategory
		c.name = unique("sub-category")
		c.displayOrder = 1
		c.published = true
		c.create()
		printTimes.foreach { case (locale, printTime) =>
			val i18n = new TestShopCategoryI18n
			i18n.id = c.id
			i18n.locale = Locale.forLanguageTag(locale)
			i18n.printTime = printTime
			i18n.create()
		}
		c
	}

	test("ShopCategoryService loads category subclass with translations") {
		val category = createSubCategory(Map("cs-CZ" -> "3 dny", "en-GB" -> "3 days"))

		withCategoryClass(withConn(implicit conn => {
			val all = ShopCategoryService.getAll()
			val loaded = all.find(_.id == category.id).get.asInstanceOf[TestShopCategory]
			assert(all.forall(_.isInstanceOf[TestShopCategory]))
			assert(loaded.name == category.name)
			assert(loaded.i18n.map(_.printTime).toSet == Set("3 dny", "3 days"))
		}))
	}

	test("category of loaded products is an instance of the category class") {
		val category = createSubCategory(Map("en-GB" -> "1 day"))
		val product = new TestShopProduct
		product.name = unique("product")
		product.price = new BigDecimal("10")
		product.sku = unique("sku")
		product.category = category
		product.published = true
		product.create()

		val original = ShopInit.productClass
		ShopInit.productClass = classOf[TestShopProduct]
		try {
			withCategoryClass(withConn(implicit conn => {
				val loaded = ShopProductService.getAll().find(_.id == product.id).get
				assert(loaded.category.isInstanceOf[TestShopCategory])
				assert(loaded.category.asInstanceOf[TestShopCategory].i18n.map(_.printTime) == Seq("1 day"))
			}))
			// without the category class the product keeps the plain category
			withConn(implicit conn => {
				val loaded = ShopProductService.getAll().find(_.id == product.id).get
				assert(!loaded.category.isInstanceOf[TestShopCategory])
			})
		} finally ShopInit.productClass = original
	}

	test("categories without subclass row are not loaded by the subclass category class") {
		val plain = createCategory()

		assert(withConn(implicit conn => ShopCategoryService.getAll()).exists(_.id == plain.id))
		withCategoryClass(withConn(implicit conn => {
			assert(!ShopCategoryService.getAll().exists(_.id == plain.id))
		}))
	}
}
