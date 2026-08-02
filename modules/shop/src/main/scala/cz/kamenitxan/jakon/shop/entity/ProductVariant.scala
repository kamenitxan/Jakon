package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.Transient
import cz.kamenitxan.jakon.core.model.{JakonObject, Ordered}
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement}

/**
 * Variant type for shop products (e.g. Color, Size).
 * Assign variants to a category via {@link ShopCategoryVariant}.
 */
class ProductVariant extends JakonObject with Ordered {

	@NotEmpty
	@JakonField(searched = true)
	var name: String = ""

	@JakonField(listOrder = -96, shownInEdit = false, shownInList = false)
	var objectOrder: Double = _

	@Transient
	@JakonField(listOrder = -96)
	var visibleOrder: Int = _

	override val objectSettings: ObjectSettings = ProductVariant.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ProductVariant (id, name, objectOrder) VALUES (?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, name)
		stmt.setDouble(3, objectOrder)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ProductVariant SET name = ?, objectOrder = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setDouble(2, objectOrder)
		stmt.setInt(3, jid)
		stmt.executeUpdate()
	}

	override def toString: String = s"ProductVariant(id: $id, $name)"
}

object ProductVariant {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-tags")
}
