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
	
	override val objectSettings: ObjectSettings = CartItem.objectSettings

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO CartItem (cart_id, product_id, quantity) VALUES (?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, cart.id)
		stmt.setInt(2, product.id)
		stmt.setInt(3, quantity)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE CartItem SET quantity = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, quantity)
		stmt.setInt(2, jid)
		stmt.executeUpdate()
	}

	override def toString: String = {
		s"CartItem(id: $id, productId: ${product.id}, qty: $quantity)"
	}
}

object CartItem {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-list", standAlone = true)
}
