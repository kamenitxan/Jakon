package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.service.ProductVariantService

import java.math.BigDecimal
import java.sql.Connection

/**
 * Seeds the empty shop database with test data.
 */
object ShopTestDataSeeder {

	/** Seeds test data when the shop has no products, categories, shipping nor payment methods. */
	def seedIfEmpty()(implicit conn: Connection): Unit = {
		if (isEmpty) {
			Logger.info("No shop data found — seeding test data")
			seedTestData()
		}
	}

	private def isEmpty(implicit conn: Connection): Boolean = {
		def count(table: String): Long = {
			DBHelper.count(s"SELECT COUNT(*) FROM $table")
		}
		count("ShopProduct") == 0 &&
		count("ShopCategory") == 0 &&
		count("ShippingMethod") == 0 &&
		count("PaymentMethod") == 0
	}

	private def seedTestData()(implicit conn: Connection): Unit = {
		// --- Kategorie ---
		val electronics = makeCategory("Elektronika", "Telefony, notebooky a příslušenství", 1)
		val clothing    = makeCategory("Oblečení",    "Trička, mikiny a bundy",              2)
		val home        = makeCategory("Dům a zahrada","Nábytek, nářadí a dekorace",         3)

		// --- Doprava ---
		makeShipping("Osobní odběr",     "Zdarma na naší prodejně",        BigDecimal.ZERO,       1)
		makeShipping("Zásilkovna",        "Doručení na výdejní místo",       new BigDecimal("79"),  2)
		makeShipping("PPL - doručení domů","Přepravní služba PPL",           new BigDecimal("129"), 3)

		// --- Platba ---
		makePayment("Kartou online",  "Visa, Mastercard",          BigDecimal.ZERO,      1, "manual")
		makePayment("Dobírka",        "Platba při převzetí",        new BigDecimal("39"), 2, "manual")
		makePayment("Bankovní převod","Platba převodem na účet",   BigDecimal.ZERO,      3, "manual")

		// --- Produkty ---
		makeProduct("Smartphone ProMax 12",  "Výkonný smartphone s AMOLED displejem, 5G a trojitým fotoaparátem.",
			"Nejprodávanější smartphone roku.", new BigDecimal("14990"), null,            "SM-PM12",  25, electronics)
		makeProduct("Bezdrátová sluchátka X", "Potlačení hluku, 30h výdrž, prémiový zvuk.",
			"Perfektní pro práci i sport.",     new BigDecimal("2490"),  new BigDecimal("1990"), "BT-SX",  40, electronics)
		makeProduct("USB-C HUB 7v1",         "HDMI 4K, USB 3.0 ×3, SD karta, PD 100W.",
			"Rozšiř svůj notebook jedním kabelem.", new BigDecimal("890"), null,         "HUB-7C",  60, electronics)
		makeProduct("Pánské tričko Classic", "100% bavlna, dostupné ve 5 barvách, unisex střih.",
			"Základní tričko do každého šatníku.", new BigDecimal("349"), new BigDecimal("249"), "TR-CL-M", 100, clothing)
		val hoodie = makeProduct("Mikina s kapucí Urban", "Bavlněný fleece, klokaní kapsa, regulovatelná kapuce.",
			"Pohodlná mikina na každý den.",   new BigDecimal("799"),  null,            "MK-URB",  50, clothing)
		makeProduct("Zahradní sada nářadí",  "12dílná sada s lopatkou, hrabičkami a konvičkou.",
			"Vše pro vaši zahradu.",            new BigDecimal("1290"), new BigDecimal("990"),  "ZS-12",   30, home)

		// --- Varianty - každá varianta je samostatný produkt s vlastní cenou ---
		val size = makeVariant("Velikost")
		val sizeM  = makeVariantValue(size, "M")
		val sizeL  = makeVariantValue(size, "L")
		val sizeXL = makeVariantValue(size, "XL")
		ProductVariantService.setVariantValues(hoodie, Seq(sizeM))
		makeProductVariant(hoodie, "L",  new BigDecimal("849"), null,                  "MK-URB-L",  20, sizeL)
		makeProductVariant(hoodie, "XL", new BigDecimal("899"), new BigDecimal("849"), "MK-URB-XL", 10, sizeXL)

		Logger.info("Test data seeded successfully")
	}

	/** Creates new product of [[ShopInit.productClass]]. */
	private def newProduct(): ShopProduct = ShopInit.productClass.getDeclaredConstructor().newInstance()

	private def makeVariant(name: String)(implicit conn: Connection): ProductVariant = {
		val v = new ProductVariant
		v.name = name
		v.published = true
		v.create()
		v
	}

	private def makeVariantValue(variant: ProductVariant, value: String)(implicit conn: Connection): ProductVariantValue = {
		val v = new ProductVariantValue
		v.variant = variant
		v.value = value
		v.published = true
		v.create()
		v
	}

	/** Variant of the main product is a standalone product with own price and stock. */
	private def makeProductVariant(main: ShopProduct, nameSuffix: String, price: BigDecimal, discountPrice: BigDecimal, sku: String,
	                               stock: Int, values: ProductVariantValue*)(implicit conn: Connection): ShopProduct = {
		val p = newProduct()
		p.name = s"${main.name} $nameSuffix"
		p.description = main.description
		p.shortDescription = main.shortDescription
		p.price = price
		p.discountPrice = discountPrice
		p.sku = sku
		p.stockQuantity = stock
		p.category = main.category
		p.mainProduct = main
		p.published = true
		p.create()
		ProductVariantService.setVariantValues(p, values)
		p
	}

	private def makeCategory(name: String, description: String, order: Int)(implicit conn: Connection): ShopCategory = {
		val c = new ShopCategory
		c.name = name
		c.description = description
		c.displayOrder = order
		c.published = true
		c.create()
		c
	}

	private def makeShipping(name: String, description: String, price: BigDecimal, order: Int)(implicit conn: Connection): ShippingMethod = {
		val s = new ShippingMethod
		s.name = name
		s.description = description
		s.price = price
		s.enabled = true
		s.displayOrder = order
		s.published = true
		s.create()
		s
	}

	private def makePayment(name: String, description: String, price: BigDecimal, order: Int, gateway: String)(implicit conn: Connection): PaymentMethod = {
		val p = new PaymentMethod
		p.name = name
		p.description = description
		p.price = price
		p.enabled = true
		p.displayOrder = order
		p.gatewayCode = gateway
		p.published = true
		p.create()
		p
	}

	private def makeProduct(name: String, description: String, shortDescription: String, price: BigDecimal, discountPrice: BigDecimal, sku: String,
	                        stock: Int, category: ShopCategory)(implicit conn: Connection): ShopProduct = {
		val p = newProduct()
		p.name = name
		p.description = description
		p.shortDescription = shortDescription
		p.price = price
		p.discountPrice = discountPrice
		p.sku = sku
		p.stockQuantity = stock
		p.category = category
		p.published = true
		p.create()
		p
	}
}
