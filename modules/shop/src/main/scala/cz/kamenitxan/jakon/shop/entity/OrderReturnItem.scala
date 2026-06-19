package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}

/**
 * A single line item within an [[OrderReturn]], representing one product being returned.
 */
class OrderReturnItem extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = OrderReturnItem.objectSettings

	/** The return request this item belongs to. */
	@ManyToOne
	@JakonField(required = true, searched = true)
	var orderReturn: OrderReturn = _

	/** The original order line item being returned. */
	@ManyToOne
	@JakonField(required = true, searched = true)
	var orderItem: ShopOrderItem = _

	/** Snapshot of the product name at the time of the return request. */
	@JakonField(searched = true)
	var productName: String = ""

	/** Number of units the customer is returning (must be ≤ original quantity). */
	@JakonField(required = true)
	var quantity: Int = 1

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql =
			"""INSERT INTO OrderReturnItem
			  |  (orderReturn_id, orderItem_id, productName, quantity, url, published)
			  |  VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		if (orderReturn != null) {
			stmt.setInt(1, orderReturn.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		if (orderItem != null) {
			stmt.setInt(2, orderItem.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setString(3, productName)
		stmt.setInt(4, quantity)
		stmt.setString(5, url)
		stmt.setBoolean(6, published)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql =
			"""UPDATE OrderReturnItem
			  |  SET orderReturn_id = ?, orderItem_id = ?, productName = ?, quantity = ?, url = ?, published = ?
			  |  WHERE id = ?""".stripMargin
		val stmt = conn.prepareStatement(sql)
		if (orderReturn != null) {
			stmt.setInt(1, orderReturn.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		if (orderItem != null) {
			stmt.setInt(2, orderItem.id)
		} else {
			stmt.setNull(2, Types.INTEGER)
		}
		stmt.setString(3, productName)
		stmt.setInt(4, quantity)
		stmt.setString(5, url)
		stmt.setBoolean(6, published)
		stmt.setInt(7, jid)
		stmt.executeUpdate()
	}
}

object OrderReturnItem {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-undo", standAlone = true)
}
