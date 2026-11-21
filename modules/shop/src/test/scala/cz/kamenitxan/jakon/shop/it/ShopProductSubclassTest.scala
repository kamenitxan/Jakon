package cz.kamenitxan.jakon.shop.it

import cz.kamenitxan.jakon.shop.ShopInit
import cz.kamenitxan.jakon.shop.service.{ProductVariantService, ShopProductService}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal
import java.util.Locale

/** Shop services work with a subclass of ShopProduct set as the product class. */
@DoNotDiscover
class ShopProductSubclassTest extends AnyFunSuite with ShopDbSupport {

	private def withProductClass[T](f: => T): T = {
		val original = ShopInit.productClass
		ShopInit.productClass = classOf[TestShopProduct]
		try f finally ShopInit.productClass = original
	}

	private def createSubProduct(subtitle: String, mainProduct: TestShopProduct = null): TestShopProduct = {
		val p = new TestShopProduct
		p.name = unique("sub-product")
		p.price = new BigDecimal("100")
		p.sku = unique("sku")
		p.stockQuantity = 3
		p.category = createCategory()
		p.mainProduct = mainProduct
		p.subtitle = subtitle
		p.published = true
		p.create()
		p
	}

	private def addTranslation(product: TestShopProduct, locale: Locale, description: String): Unit = {
		val i18n = new TestShopProductI18n
		i18n.id = product.id
		i18n.locale = locale
		i18n.description = description
		i18n.create()
	}

	test("ShopProductService loads product subclass with own fields and translations") {
		val product = createSubProduct("sub")
		addTranslation(product, Locale.forLanguageTag("cs-CZ"), "popis")
		addTranslation(product, Locale.forLanguageTag("en-GB"), "description")

		withProductClass(withConn(implicit conn => {
			val all = ShopProductService.getAll()
			val loaded = all.find(_.id == product.id).get.asInstanceOf[TestShopProduct]
			assert(all.forall(_.isInstanceOf[TestShopProduct]))
			assert(loaded.name == product.name)
			assert(loaded.subtitle == "sub")
			assert(loaded.i18n.map(_.description).toSet == Set("popis", "description"))
			assert(ShopProductService.getListed().exists(_.id == product.id))
		}))
	}

	test("subclass update keeps ShopProduct and subclass columns in sync") {
		val product = createSubProduct("before")
		product.name = "renamed"
		product.subtitle = "after"
		product.update()

		withProductClass(withConn(implicit conn => {
			val loaded = ShopProductService.getAll().find(_.id == product.id).get.asInstanceOf[TestShopProduct]
			assert(loaded.name == "renamed")
			assert(loaded.subtitle == "after")
		}))
	}

	test("loadVariantGroup returns subclass instances and getListed hides variants") {
		val main = createSubProduct("main")
		val variant = createSubProduct("variant", main)

		withProductClass(withConn(implicit conn => {
			val group = ProductVariantService.loadVariantGroup(main)
			assert(group.map(_.id) == Seq(main.id, variant.id))
			assert(group.forall(_.isInstanceOf[TestShopProduct]))
			val listed = ShopProductService.getListed().map(_.id)
			assert(listed.contains(main.id) && !listed.contains(variant.id))
		}))
	}

	test("products without subclass row are not loaded by the subclass product class") {
		val plain = createProduct()

		assert(withConn(implicit conn => ShopProductService.getAll()).exists(_.id == plain.id))
		withProductClass(withConn(implicit conn => {
			assert(!ShopProductService.getAll().exists(_.id == plain.id))
		}))
	}
}
