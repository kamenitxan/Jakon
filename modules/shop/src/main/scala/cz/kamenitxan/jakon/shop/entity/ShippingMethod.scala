package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement}

/**
 * Způsob dopravy
 */
class ShippingMethod extends JakonObject with Serializable {

	@NotEmpty
	@JakonField(searched = true)
	var name: String = ""
	
	@JakonField(required = false)
	var description: String = ""
	
	@JakonField()
	var price: BigDecimal = BigDecimal.ZERO
	
	@JakonField(searched = true)
	var enabled: Boolean = true
	
	@JakonField()
	var displayOrder: Int = 0
	
	@JakonField(required = false)
	var icon: String = ""
	
	@JakonField(required = false)
	var estimatedDeliveryDays: Int = 0

	override val objectSettings: ObjectSettings = ShippingMethod.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShippingMethod (id, name, description, price, enabled, displayOrder, icon, estimatedDeliveryDays, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setString(3, description)
		stmt.setBigDecimal(4, price)
		stmt.setBoolean(5, enabled)
		stmt.setInt(6, displayOrder)
		stmt.setString(7, icon)
		stmt.setInt(8, estimatedDeliveryDays)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)

		executeInsert(stmt)
	}

	override def toString: String = {
		s"ShippingMethod(id: $id, $name)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShippingMethod SET name = ?, description = ?, price = ?, enabled = ?, displayOrder = ?, icon = ?, estimatedDeliveryDays = ?, url = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setString(2, description)
		stmt.setBigDecimal(3, price)
		stmt.setBoolean(4, enabled)
		stmt.setInt(5, displayOrder)
		stmt.setString(6, icon)
		stmt.setInt(7, estimatedDeliveryDays)
		stmt.setString(8, url)
		stmt.setBoolean(9, published)
		stmt.setInt(10, jid)
		stmt.executeUpdate()
	}
}

object ShippingMethod {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-truck")
}

