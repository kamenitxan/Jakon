package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.entity.*

import java.math.BigDecimal
import java.sql.Connection

class ShopInit extends JakonInit {

	override def daoSetup(): Unit = {
		DBHelper.addDao(classOf[PaymentMethod])
		DBHelper.addDao(classOf[ShippingMethod])
		DBHelper.addDao(classOf[ShopCategory])
		DBHelper.addDao(classOf[ShopProduct])
		DBHelper.addDao(classOf[Cart])
		DBHelper.addDao(classOf[CartItem])
		DBHelper.addDao(classOf[ShopOrder])
		DBHelper.addDao(classOf[ShopOrderItem])
	}

	override protected def afterInit(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			if (isEmpty) {
				Logger.info("No shop data found — seeding test data")
				seedTestData()
			}
		})
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
		makeProduct("Mikina s kapucí Urban", "Bavlněný fleece, klokaní kapsa, regulovatelná kapuce.",
			"Pohodlná mikina na každý den.",   new BigDecimal("799"),  null,            "MK-URB",  50, clothing)
		makeProduct("Zahradní sada nářadí",  "12dílná sada s lopatkou, hrabičkami a konvičkou.",
			"Vše pro vaši zahradu.",            new BigDecimal("1290"), new BigDecimal("990"),  "ZS-12",   30, home)

		Logger.info("Test data seeded successfully")
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
		val p = new ShopProduct
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