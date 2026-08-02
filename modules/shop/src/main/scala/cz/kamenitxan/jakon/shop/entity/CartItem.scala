package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement}

/**
 * Položka dočasného košíku
 */
class CartItem extends JakonObject with Serializable {

	@JakonField()
	@ManyToOne
	var cart: Cart = _

	@JakonField()
	@ManyToOne
	var product: ShopProduct = _

	@JakonField
	var quantity: Int = 1

	/** Comma-separated selected ProductVariantValue IDs, e.g. "3,7". May be null. */
	@JakonField(required = false)
	var variantSelection: String = _

	override val objectSettings: ObjectSettings = CartItem.objectSettings

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO CartItem (cart_id, product_id, quantity, variantSelection) VALUES (?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, cart.id)
		stmt.setInt(2, product.id)
		stmt.setInt(3, quantity)
		if (variantSelection != null) stmt.setString(4, variantSelection) else stmt.setNull(4, java.sql.Types.VARCHAR)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE CartItem SET quantity = ?, variantSelection = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, quantity)
		if (variantSelection != null) stmt.setString(2, variantSelection) else stmt.setNull(2, java.sql.Types.VARCHAR)
		stmt.setInt(3, jid)
		stmt.executeUpdate()
	}

	override def toString: String = {
		s"CartItem(id: $id, productId: ${product.id}, qty: $quantity)"
	}
}

object CartItem {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-list", standAlone = true)
}
