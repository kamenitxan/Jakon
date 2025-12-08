package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement, Types}

/**
 * Položka objednávky
 */
class OrderItem extends JakonObject with Serializable {

	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var order: Order = _
	
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var product: ShopProduct = _
	
	@JakonField(searched = true)
	var productName: String = ""
	
	@NotEmpty
	@JakonField(searched = true)
	var quantity: Int = 1
	
	@NotEmpty
	@JakonField(searched = true)
	var unitPrice: BigDecimal = BigDecimal.ZERO
	
	@NotEmpty
	@JakonField(searched = true)
	var totalPrice: BigDecimal = BigDecimal.ZERO
	
	@JakonField(required = false)
	var note: String = ""

	override val objectSettings: ObjectSettings = OrderItem.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO OrderItem (id, order_id, product_id, productName, quantity, unitPrice, totalPrice, note, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		if (order != null) {
			stmt.setInt(2, order.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		if (product != null) {
			stmt.setInt(3, product.id)
		} else {
			stmt.setNull(3, Types.INTEGER)
		}
		stmt.setString(4, productName)
		stmt.setInt(5, quantity)
		stmt.setBigDecimal(6, unitPrice)
		stmt.setBigDecimal(7, totalPrice)
		stmt.setString(8, note)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)

		executeInsert(stmt)
	}

	override def toString: String = {
		s"OrderItem(id: $id, product: $productName, qty: $quantity)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE OrderItem SET order_id = ?, product_id = ?, productName = ?, quantity = ?, unitPrice = ?, totalPrice = ?, note = ?, url = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		if (order != null) {
			stmt.setInt(1, order.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		if (product != null) {
			stmt.setInt(2, product.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setString(3, productName)
		stmt.setInt(4, quantity)
		stmt.setBigDecimal(5, unitPrice)
		stmt.setBigDecimal(6, totalPrice)
		stmt.setString(7, note)
		stmt.setString(8, url)
		stmt.setBoolean(9, published)
		stmt.setInt(10, jid)
		stmt.executeUpdate()
	}
}

object OrderItem {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-list", standAlone = true)
}

