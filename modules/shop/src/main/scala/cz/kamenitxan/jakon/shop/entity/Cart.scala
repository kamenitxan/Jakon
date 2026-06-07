package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Timestamp, Types}
import java.time.LocalDateTime

/**
 * Dočasný košík zákazníka (identifikovaný UUID tokenem v cookie)
 */
class Cart extends JakonObject with Serializable {

	@JakonField(searched = true)
	var token: String = ""

	@ManyToOne
	@JakonField(required = false)
	var selectedShippingMethod: ShippingMethod = _

	@ManyToOne
	@JakonField(required = false)
	var selectedPaymentMethod: PaymentMethod = _

	@JakonField
	var createdAt: LocalDateTime = LocalDateTime.now()

	@JakonField
	var updatedAt: LocalDateTime = LocalDateTime.now()

	override val objectSettings: ObjectSettings = Cart.objectSettings

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO Cart (token, selectedShippingMethod_id, selectedPaymentMethod_id, createdAt, updatedAt, url, published) VALUES (?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setString(1, token)
		if (selectedShippingMethod != null) {
			stmt.setInt(2, selectedShippingMethod.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		if (selectedPaymentMethod != null) {
			stmt.setInt(3, selectedPaymentMethod.id)
		} else {
			stmt.setNull(3, Types.INTEGER)
		}
		stmt.setObject(4, createdAt)
		stmt.setObject(5, updatedAt)
		stmt.setString(6, url)
		stmt.setBoolean(7, published)

		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE Cart SET selectedShippingMethod_id = ?, selectedPaymentMethod_id = ?, updatedAt = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		if (selectedShippingMethod != null) {
			stmt.setInt(1, selectedShippingMethod.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		if (selectedPaymentMethod != null) {
			stmt.setInt(2, selectedPaymentMethod.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()))
		stmt.setInt(4, jid)
		stmt.executeUpdate()
	}

	override def toString: String = {
		s"Cart(id: $id, token: $token)"
	}
}

object Cart {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-shopping-cart", standAlone = true)
}
