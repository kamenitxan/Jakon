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
class ShopOrderItem extends JakonObject with Serializable {

	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var shopOrder: ShopOrder = _
	
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

	/** Human-readable variant selection snapshot, e.g. "Barva: Červená, Velikost: M". May be null. */
	@JakonField(required = false)
	var variantSelection: String = _

	override val objectSettings: ObjectSettings = ShopOrderItem.objectSettings

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShopOrderItem (order_id, product_id, productName, quantity, unitPrice, totalPrice, note, variantSelection, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		if (shopOrder != null) {
			stmt.setInt(1, shopOrder.id)
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
		if (variantSelection != null) stmt.setString(8, variantSelection) else stmt.setNull(8, Types.VARCHAR)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def toString: String = {
		s"OrderItem(id: $id, product: $productName, qty: $quantity)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShopOrderItem SET order_id = ?, product_id = ?, productName = ?, quantity = ?, unitPrice = ?, totalPrice = ?, note = ?, variantSelection = ?, url = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		if (shopOrder != null) {
			stmt.setInt(1, shopOrder.id)
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
		if (variantSelection != null) stmt.setString(8, variantSelection) else stmt.setNull(8, Types.VARCHAR)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)
		stmt.setInt(11, jid)
		stmt.executeUpdate()
	}
}

object ShopOrderItem {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-list", standAlone = true)
}

