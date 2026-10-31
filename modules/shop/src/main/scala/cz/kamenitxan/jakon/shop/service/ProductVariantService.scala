package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopInit
import cz.kamenitxan.jakon.shop.entity.{ProductVariant, ProductVariantValue, ShopProduct}

import java.math.BigDecimal
import java.sql.Connection
import scala.jdk.CollectionConverters.*

/**
 * Product variants. Every variant is a standalone [[ShopProduct]] with its own price, variants of one product
 * reference the same main product ([[ShopProduct#mainProduct]]) and differ by [[ShopProduct#variantValues]].
 */
object ProductVariantService {

	/**
	 * Builds a human-readable description of product variant from its variant values,
	 * e.g. "Barva: Červená, Velikost: M". Returns empty string for product without variant values.
	 */
	def buildReadableVariant(productId: Int)(implicit conn: Connection): String = {
		// language=SQL
		val sql =
			"""SELECT pvv.value, pv.name AS variantName
			  |FROM ShopProductProductVariantValue j
			  |JOIN ProductVariantValue pvv ON pvv.id = j.productVariantValue_id
			  |JOIN ProductVariant pv ON pv.id = pvv.variant_id
			  |WHERE j.shopProduct_id = ?
			  |ORDER BY pv.objectOrder, pvv.objectOrder""".stripMargin
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, productId)
		val rs = stmt.executeQuery()
		val parts = scala.collection.mutable.ArrayBuffer.empty[String]
		while (rs.next()) {
			parts += s"${rs.getString("variantName")}: ${rs.getString("value")}"
		}
		rs.close()
		stmt.close()
		parts.mkString(", ")
	}

	/**
	 * Replaces variant values of the product. Admin saves them itself, this is for creating variants from code.
	 */
	def setVariantValues(product: ShopProduct, values: Seq[ProductVariantValue])(implicit conn: Connection): Unit = {
		val delete = conn.prepareStatement("DELETE FROM ShopProductProductVariantValue WHERE shopProduct_id = ?")
		delete.setInt(1, product.id)
		delete.executeUpdate()
		delete.close()
		values.foreach(value => {
			val insert = conn.prepareStatement("INSERT INTO ShopProductProductVariantValue (shopProduct_id, productVariantValue_id) VALUES (?, ?)")
			insert.setInt(1, product.id)
			insert.setInt(2, value.id)
			insert.executeUpdate()
			insert.close()
		})
		product.variantValues = values
	}

	/**
	 * Returns all published products of the variant group of the given product (main product and its variants),
	 * with loaded variant values. Product without variants returns only itself.
	 */
	def loadVariantGroup(product: ShopProduct)(implicit conn: Connection): Seq[ShopProduct] = {
		implicit val cls: Class[ShopProduct] = ShopInit.productClass.asInstanceOf[Class[ShopProduct]]
		// language=SQL
		val sql =
			s"""SELECT * FROM ${ShopProductService.fromClause(cls)}
			  |WHERE (ShopProduct.id = ? OR ShopProduct.mainProduct_id = ?) AND JakonObject.published = true
			  |ORDER BY ShopProduct.id""".stripMargin
		val stmt = conn.prepareStatement(sql)
		val groupId = product.variantGroupId
		stmt.setInt(1, groupId)
		stmt.setInt(2, groupId)
		val products = DBHelper.selectDeep(stmt)
		stmt.close()
		products
	}

	/**
	 * Creates variant selector of product detail. Axes are variants used by products of the group, values of the
	 * given product are selected. Returns None for product which is not part of any variant group.
	 */
	def buildSelector(product: ShopProduct, group: Seq[ShopProduct]): Option[VariantSelector] = {
		val withValues = group.filter(_.variantValues.nonEmpty)
		if (withValues.size < 2) {
			return None
		}
		val selectedIds = product.variantValues.map(_.id).toSet
		val values = withValues.flatMap(_.variantValues).distinctBy(_.id)
		val axes = values
			.filter(_.variant != null)
			.groupBy(_.variant.id)
			.values
			.toSeq
			.sortBy(vs => (vs.head.variant.objectOrder, vs.head.variant.id))
			.map(vs => VariantAxis(
				vs.head.variant,
				vs.sortBy(v => (v.objectOrder, v.id)).map(v => VariantAxisValue(v, selectedIds.contains(v.id))).asJava
			))
		val options = withValues.map(p => VariantOption(
			productId = p.id,
			valueIds = p.variantValues.map(_.id).sorted.toArray,
			price = p.price,
			discountPrice = p.discountPrice,
			stockQuantity = p.stockQuantity
		))
		Some(VariantSelector(axes.asJava, options.toArray))
	}
}

/** Variant selector of product detail, Pebble can iterate only java collections. */
case class VariantSelector(axes: java.util.List[VariantAxis], options: Array[VariantOption])

/** One variant (e.g. Color) with values used in variant group. */
case class VariantAxis(variant: ProductVariant, values: java.util.List[VariantAxisValue])

case class VariantAxisValue(value: ProductVariantValue, selected: Boolean)

/** Product of variant group, serialized to JSON for switching variants on product detail. */
case class VariantOption(productId: Int, valueIds: Array[Int], price: BigDecimal, discountPrice: BigDecimal, stockQuantity: Int)
