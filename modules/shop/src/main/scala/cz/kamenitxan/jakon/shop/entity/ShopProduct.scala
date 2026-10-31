package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.{ManyToMany, ManyToOne}
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.validation.validators.NotEmpty
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, PreparedStatement, Statement, Types}

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
	var discountPrice: BigDecimal = scala.compiletime.uninitialized
	
	@JakonField
	var stockQuantity: Int = 0
	
	@JakonField(searched = true)
	var sku: String = ""
	
	@JakonField(required = false)
	var mainImage: String = ""
	
	@JakonField(required = false)
	var images: String = ""
	
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var category: ShopCategory = scala.compiletime.uninitialized
	
	@JakonField(searched = true)
	var featured: Boolean = false

	/**
	 * Main product of variant group. Every variant is a standalone product with its own price, empty for product
	 * without variants and for the main product of the group.
	 */
	@ManyToOne
	@JakonField(required = false, searched = true)
	var mainProduct: ShopProduct = scala.compiletime.uninitialized

	/** Variant values distinguishing this product within its variant group, e.g. Color: Red and Size: XL. */
	@ManyToMany(genericClass = classOf[ProductVariantValue])
	@JakonField(required = false)
	var variantValues: Seq[ProductVariantValue] = Seq.empty

	override val objectSettings: ObjectSettings = ShopProduct.objectSettings

	override def toString: String = {
		s"ShopProduct(id: $id, $name, price: $price)"
	}

	/** id of the main product of the variant group, own id for product without variants */
	def variantGroupId: Int = Option(mainProduct).map(_.id).getOrElse(id)

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO ShopProduct (id, name, description, shortDescription, price, discountPrice, stockQuantity, sku, mainImage, images, category_id, featured, mainProduct_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		setValues(stmt, 2)
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE ShopProduct SET name = ?, description = ?, shortDescription = ?, price = ?, discountPrice = ?, stockQuantity = ?, sku = ?, mainImage = ?, images = ?, category_id = ?, featured = ?, mainProduct_id = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		setValues(stmt, 1)
		stmt.setInt(13, jid)
		stmt.executeUpdate()
	}

	/** Sets values of all columns except id, starting at parameter index from. */
	private def setValues(stmt: PreparedStatement, from: Int): Unit = {
		stmt.setString(from, name)
		stmt.setString(from + 1, description)
		stmt.setString(from + 2, shortDescription)
		stmt.setBigDecimal(from + 3, price)
		if (discountPrice != null) {
			stmt.setBigDecimal(from + 4, discountPrice)
		} else {
			stmt.setNull(from + 4, Types.DECIMAL)
		}
		stmt.setInt(from + 5, stockQuantity)
		stmt.setString(from + 6, sku)
		stmt.setString(from + 7, mainImage)
		stmt.setString(from + 8, images)
		if (category != null) {
			stmt.setInt(from + 9, category.id)
		} else {
			stmt.setNull(from + 9, Types.INTEGER)
		}
		stmt.setBoolean(from + 10, featured)
		if (mainProduct != null) {
			stmt.setInt(from + 11, mainProduct.id)
		} else {
			stmt.setNull(from + 11, Types.INTEGER)
		}
	}
}

object ShopProduct {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-shopping-bag")
}

