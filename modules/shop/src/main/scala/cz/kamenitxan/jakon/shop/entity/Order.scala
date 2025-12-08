package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement, Timestamp, Types}
import java.time.LocalDateTime

/**
 * Objednávka
 */
class Order extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = Order.objectSettings

	@NotEmpty
	@JakonField(searched = true)
	var orderNumber: String = ""
	
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var customer: Customer = _
	
	@JakonField(searched = true)
	var orderDate: LocalDateTime = LocalDateTime.now()
	
	@NotEmpty
	@JakonField(searched = true)
	var status: String = "NEW" // NEW, PROCESSING, SHIPPED, DELIVERED, CANCELLED
	
	@NotEmpty
	@JakonField(searched = true)
	var totalPrice: BigDecimal = BigDecimal.ZERO
	
	@JakonField(searched = true)
	var shippingPrice: BigDecimal = BigDecimal.ZERO
	
	@JakonField(searched = true)
	var paymentPrice: BigDecimal = BigDecimal.ZERO
	
	@ManyToOne
	@JakonField(required = false, searched = true)
	var paymentMethod: PaymentMethod = _
	
	@ManyToOne
	@JakonField(required = false, searched = true)
	var shippingMethod: ShippingMethod = _
	
	@JakonField(required = false)
	var customerNote: String = ""
	
	@JakonField(required = false)
	var adminNote: String = ""
	
	// Fakturační adresa
	@JakonField(required = false)
	var billingName: String = ""
	
	@JakonField(required = false)
	var billingStreet: String = ""
	
	@JakonField(required = false)
	var billingCity: String = ""
	
	@JakonField(required = false)
	var billingZip: String = ""
	
	@JakonField(required = false)
	var billingCountry: String = ""
	
	// Dodací adresa
	@JakonField(required = false)
	var deliveryName: String = ""
	
	@JakonField(required = false)
	var deliveryStreet: String = ""
	
	@JakonField(required = false)
	var deliveryCity: String = ""
	
	@JakonField(required = false)
	var deliveryZip: String = ""
	
	@JakonField(required = false)
	var deliveryCountry: String = ""
	
	@JakonField(searched = true)
	var isPaid: Boolean = false
	
	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO `Order` (id, orderNumber, customer_id, orderDate, status, totalPrice, shippingPrice, paymentPrice, paymentMethod_id, shippingMethod_id, customerNote, adminNote, billingName, billingStreet, billingCity, billingZip, billingCountry, deliveryName, deliveryStreet, deliveryCity, deliveryZip, deliveryCountry, isPaid, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, orderNumber)
		if (customer != null) {
			stmt.setInt(3, customer.id)
		} else {
			stmt.setNull(3, Types.INTEGER)
		}
		stmt.setTimestamp(4, Timestamp.valueOf(orderDate))
		stmt.setString(5, status)
		stmt.setBigDecimal(6, totalPrice)
		stmt.setBigDecimal(7, shippingPrice)
		stmt.setBigDecimal(8, paymentPrice)
		if (paymentMethod != null) {
			stmt.setInt(9, paymentMethod.id)
		} else {
			stmt.setNull(9, Types.INTEGER)
		}
		if (shippingMethod != null) {
			stmt.setInt(10, shippingMethod.id)
		} else {
			stmt.setNull(10, Types.INTEGER)
		}
		stmt.setString(11, customerNote)
		stmt.setString(12, adminNote)
		stmt.setString(13, billingName)
		stmt.setString(14, billingStreet)
		stmt.setString(15, billingCity)
		stmt.setString(16, billingZip)
		stmt.setString(17, billingCountry)
		stmt.setString(18, deliveryName)
		stmt.setString(19, deliveryStreet)
		stmt.setString(20, deliveryCity)
		stmt.setString(21, deliveryZip)
		stmt.setString(22, deliveryCountry)
		stmt.setBoolean(23, isPaid)
		stmt.setString(24, url)
		stmt.setBoolean(25, published)

		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE `Order` SET orderNumber = ?, customer_id = ?, orderDate = ?, status = ?, totalPrice = ?, shippingPrice = ?, paymentPrice = ?, paymentMethod_id = ?, shippingMethod_id = ?, customerNote = ?, adminNote = ?, billingName = ?, billingStreet = ?, billingCity = ?, billingZip = ?, billingCountry = ?, deliveryName = ?, deliveryStreet = ?, deliveryCity = ?, deliveryZip = ?, deliveryCountry = ?, isPaid = ?, url = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, orderNumber)
		if (customer != null) {
			stmt.setInt(2, customer.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setTimestamp(3, Timestamp.valueOf(orderDate))
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
		stmt.setBoolean(22, isPaid)
		stmt.setString(23, url)
		stmt.setBoolean(24, published)
		stmt.setInt(25, jid)
		stmt.executeUpdate()
	}
}

object Order {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-shopping-cart", standAlone = true)
}

