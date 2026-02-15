package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement, Types}

/**
 * Produkt v e-shopu
 */
class ShopProduct extends JakonObject with Serializable {

	@NotEmpty
	@JakonField(searched = true)
	var name: String = ""
	
	@JakonField(required = false)
	var description: String = ""
	
	@JakonField(required = false)
	var shortDescription: String = ""
	
	@NotEmpty
	@JakonField
	var price: BigDecimal = BigDecimal.ZERO
	
	@JakonField(required = false)
	var discountPrice: BigDecimal = _
	
	@JakonField
	var stockQuantity: Int = 0
	
	@JakonField(searched = true)
	var sku: String = ""
	
	@JakonField(required = false)
	var image: String = ""
	
	@JakonField(required = false)
	var images: String = ""
	
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var category: ShopCategory = _
	
	@JakonField(searched = true)
	var featured: Boolean = false
	

	override val objectSettings: ObjectSettings = ShopProduct.objectSettings

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShopProduct (name, description, shortDescription, price, discountPrice, stockQuantity, sku, image, images, category_id, featured, url, published) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setString(1, name)
		stmt.setString(2, description)
		stmt.setString(3, shortDescription)
		stmt.setBigDecimal(4, price)
		if (discountPrice != null) {
			stmt.setBigDecimal(5, discountPrice)
		} else {
			stmt.setNull(5, Types.DECIMAL)
		}
		stmt.setInt(6, stockQuantity)
		stmt.setString(7, sku)
		stmt.setString(8, image)
		stmt.setString(9, images)
		if (category != null) {
			stmt.setInt(10, category.id)
		} else {
			stmt.setNull(10, Types.INTEGER)
		}
		stmt.setBoolean(11, featured)
		stmt.setString(12, url)
		stmt.setBoolean(13, published)

		executeInsert(stmt)
	}

	override def toString: String = {
		s"ShopProduct(id: $id, $name, price: $price)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShopProduct SET name = ?, description = ?, shortDescription = ?, price = ?, discountPrice = ?, stockQuantity = ?, sku = ?, image = ?, images = ?, category_id = ?, featured = ?, displayOrder = ?, published = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		stmt.setString(2, description)
		stmt.setString(3, shortDescription)
		stmt.setBigDecimal(4, price)
		if (discountPrice != null) {
			stmt.setBigDecimal(5, discountPrice)
		} else {
			stmt.setNull(5, Types.DECIMAL)
		}
		stmt.setInt(6, stockQuantity)
		stmt.setString(7, sku)
		stmt.setString(8, image)
		stmt.setString(9, images)
		if (category != null) {
			stmt.setInt(10, category.id)
		} else {
			stmt.setNull(10, Types.INTEGER)
		}
		stmt.setBoolean(11, featured)
		stmt.setBoolean(12, published)
		stmt.setInt(13, jid)
		stmt.executeUpdate()
	}
}

object ShopProduct {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-shopping-bag", standAlone = true)
}

