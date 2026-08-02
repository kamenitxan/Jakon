package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.{ManyToOne, Transient}
import cz.kamenitxan.jakon.core.model.{JakonObject, Ordered}
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement}

/**
 * A concrete option within a {@link ProductVariant} (e.g. "Red" in "Color", "XL" in "Size").
 */
class ProductVariantValue extends JakonObject with Ordered {

	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var variant: ProductVariant = _

	@NotEmpty
	@JakonField(searched = true)
	var value: String = ""

	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	var objectOrder: Double = _
	
	@Transient
	@JakonField(listOrder = -96)
	var visibleOrder: Int = _


	override val objectSettings: ObjectSettings = ProductVariantValue.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ProductVariantValue (id, variant_id, value, objectOrder) VALUES (?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		if (variant != null) stmt.setInt(2, variant.id) else stmt.setNull(2, java.sql.Types.INTEGER)
		stmt.setString(3, value)
		stmt.setDouble(4, objectOrder)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ProductVariantValue SET variant_id = ?, value = ?, objectOrder = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		if (variant != null) stmt.setInt(1, variant.id) else stmt.setNull(1, java.sql.Types.INTEGER)
		stmt.setString(2, value)
		stmt.setDouble(3, objectOrder)
		stmt.setInt(4, jid)
		stmt.executeUpdate()
	}

	override def toString: String = s"ProductVariantValue(id: $id, variant: ${if (variant != null) variant.name else "?"}, value: $value)"
	
}

object ProductVariantValue {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-tag")
}
