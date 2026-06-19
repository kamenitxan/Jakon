package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement, Types}
import java.time.LocalDateTime

/**
 * Objednávka v e-shopu.
 */
class ShopOrder extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = ShopOrder.objectSettings

	/** Unikátní číslo objednávky (generované při vytvoření). */
	@NotEmpty
	@JakonField(searched = true)
	var orderNumber: String = ""

	/** Zákazník, který objednávku vytvořil. Může být null u host-objednávek. */
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var customer: Customer = _

	/** Datum a čas vytvoření objednávky. */
	@JakonField(searched = true)
	var orderDate: LocalDateTime = LocalDateTime.now()

	/** Aktuální stav objednávky. Možné hodnoty: NEW, PROCESSING, SHIPPED, DELIVERED, CANCELLED. */
	@NotEmpty
	@JakonField(searched = true)
	var status: String = "NEW"

	/** Celková cena objednávky včetně dopravy a platby. */
	@NotEmpty
	@JakonField(searched = true)
	var totalPrice: BigDecimal = BigDecimal.ZERO

	/** Cena dopravy. */
	@JakonField(searched = true)
	var shippingPrice: BigDecimal = BigDecimal.ZERO

	/** Cena zvoleného způsobu platby. */
	@JakonField(searched = true)
	var paymentPrice: BigDecimal = BigDecimal.ZERO

	/** Zvolený způsob platby. */
	@ManyToOne
	@JakonField(required = false, searched = true)
	var paymentMethod: PaymentMethod = _

	/** Zvolený způsob dopravy. */
	@ManyToOne
	@JakonField(required = false, searched = true)
	var shippingMethod: ShippingMethod = _

	/** Poznámka zákazníka k objednávce. */
	@JakonField(required = false)
	var customerNote: String = ""

	/** Interní poznámka administrátora, není viditelná zákazníkovi. */
	@JakonField(required = false)
	var adminNote: String = ""

	// Fakturační adresa
	/** Jméno a příjmení / název firmy na fakturační adrese. */
	@JakonField(required = false)
	var billingName: String = ""

	/** Ulice a číslo popisné fakturační adresy. */
	@JakonField(required = false)
	var billingStreet: String = ""

	/** Město fakturační adresy. */
	@JakonField(required = false)
	var billingCity: String = ""

	/** PSČ fakturační adresy. */
	@JakonField(required = false)
	var billingZip: String = ""

	/** Země fakturační adresy. */
	@JakonField(required = false)
	var billingCountry: String = ""

	// Dodací adresa
	/** Jméno a příjmení / název firmy na dodací adrese. */
	@JakonField(required = false)
	var deliveryName: String = ""

	/** Ulice a číslo popisné dodací adresy. */
	@JakonField(required = false)
	var deliveryStreet: String = ""

	/** Město dodací adresy. */
	@JakonField(required = false)
	var deliveryCity: String = ""

	/** PSČ dodací adresy. */
	@JakonField(required = false)
	var deliveryZip: String = ""

	/** Země dodací adresy. */
	@JakonField(required = false)
	var deliveryCountry: String = ""

	/** E-mail address for guest orders (without registration). */
	@JakonField(required = false, searched = true)
	var guestEmail: String = ""

	/** Telefonní číslo hosta pro objednávky bez registrace. */
	@JakonField(required = false)
	var guestPhone: String = ""

	/** Bezpečnostní token pro přístup k objednávce bez přihlášení (např. v potvrzovacím e-mailu). */
	@JakonField(required = false)
	var token: String = ""

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShopOrder (orderNumber, customer_id, orderDate, status, totalPrice, shippingPrice, paymentPrice, paymentMethod_id, shippingMethod_id, customerNote, adminNote, billingName, billingStreet, billingCity, billingZip, billingCountry, deliveryName, deliveryStreet, deliveryCity, deliveryZip, deliveryCountry, guestEmail, guestPhone, token, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setString(1, orderNumber)
		if (customer != null) {
			stmt.setInt(2, customer.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setObject(3, orderDate)
		stmt.setString(4, status)
		stmt.setBigDecimal(5, totalPrice)
		stmt.setBigDecimal(6, shippingPrice)
		stmt.setBigDecimal(7, paymentPrice)
		if (paymentMethod != null) {
			stmt.setInt(8, paymentMethod.id)
		} else {
			stmt.setNull(8, Types.INTEGER)
		}
		if (shippingMethod != null) {
			stmt.setInt(9, shippingMethod.id)
		} else {
			stmt.setNull(9, Types.INTEGER)
		}
		stmt.setString(10, customerNote)
		stmt.setString(11, adminNote)
		stmt.setString(12, billingName)
		stmt.setString(13, billingStreet)
		stmt.setString(14, billingCity)
		stmt.setString(15, billingZip)
		stmt.setString(16, billingCountry)
		stmt.setString(17, deliveryName)
		stmt.setString(18, deliveryStreet)
		stmt.setString(19, deliveryCity)
		stmt.setString(20, deliveryZip)
		stmt.setString(21, deliveryCountry)
		stmt.setString(22, guestEmail)
		stmt.setString(23, guestPhone)
		stmt.setString(24, token)
		stmt.setString(25, url)
		stmt.setBoolean(26, published)

		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShopOrder SET orderNumber = ?, customer_id = ?, orderDate = ?, status = ?, totalPrice = ?, shippingPrice = ?, paymentPrice = ?, paymentMethod_id = ?, shippingMethod_id = ?, customerNote = ?, adminNote = ?, billingName = ?, billingStreet = ?, billingCity = ?, billingZip = ?, billingCountry = ?, deliveryName = ?, deliveryStreet = ?, deliveryCity = ?, deliveryZip = ?, deliveryCountry = ?, guestEmail = ?, guestPhone = ?, token = ?, url = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, orderNumber)
		if (customer != null) {
			stmt.setInt(2, customer.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setObject(3, orderDate)
		stmt.setString(4, status)
		stmt.setBigDecimal(5, totalPrice)
		stmt.setBigDecimal(6, shippingPrice)
		stmt.setBigDecimal(7, paymentPrice)
		if (paymentMethod != null) {
			stmt.setInt(8, paymentMethod.id)
		} else {
			stmt.setNull(8, Types.INTEGER)
		}
		if (shippingMethod != null) {
			stmt.setInt(9, shippingMethod.id)
		} else {
			stmt.setNull(9, Types.INTEGER)
		}
		stmt.setString(10, customerNote)
		stmt.setString(11, adminNote)
		stmt.setString(12, billingName)
		stmt.setString(13, billingStreet)
		stmt.setString(14, billingCity)
		stmt.setString(15, billingZip)
		stmt.setString(16, billingCountry)
		stmt.setString(17, deliveryName)
		stmt.setString(18, deliveryStreet)
		stmt.setString(19, deliveryCity)
		stmt.setString(20, deliveryZip)
		stmt.setString(21, deliveryCountry)
		stmt.setString(22, guestEmail)
		stmt.setString(23, guestPhone)
		stmt.setString(24, token)
		stmt.setString(25, url)
		stmt.setBoolean(26, published)
		stmt.setInt(27, jid)
		stmt.executeUpdate()
	}


	override def toString = s"ShopOrder($orderNumber)"
}

object ShopOrder {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-shopping-cart", standAlone = true)
}

