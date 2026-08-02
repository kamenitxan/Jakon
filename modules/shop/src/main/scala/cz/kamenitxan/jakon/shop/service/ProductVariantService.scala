package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.{ProductVariant, ProductVariantValue}

import java.sql.Connection

object ProductVariantService {
	
	/**
	 * Returns all values for a given variant, ordered by displayOrder.
	 */
	def loadValuesForVariant(variantId: Int)(implicit conn: Connection): Seq[ProductVariantValue] = {
		// language=SQL
		val sql =
			"""SELECT pvv.*, jo.published, jo.url, jo.childClass
			  |FROM ProductVariantValue pvv
			  |JOIN JakonObject jo ON jo.id = pvv.id
			  |WHERE pvv.variant_id = ? AND jo.published = true
			  |ORDER BY pvv.objectOrder""".stripMargin
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, variantId)
		val values = DBHelper.selectDeep(stmt)(conn, classOf[ProductVariantValue])
		stmt.close()
		values
	}



	/**
	 * Builds a human-readable variant selection string from a comma-separated list of
	 * ProductVariantValue IDs, e.g. "Barva: Červená, Velikost: M".
	 * Returns empty string if selection is null or empty.
	 */
	def buildReadableSelection(variantSelection: String)(implicit conn: Connection): String = {
		if (variantSelection == null || variantSelection.trim.isEmpty) return ""
		val valueIds = variantSelection.split(",").flatMap(s => scala.util.Try(s.trim.toInt).toOption)
		if (valueIds.isEmpty) return ""
		val placeholders = valueIds.map(_ => "?").mkString(", ")
		// language=SQL
		val sql =
			s"""SELECT pvv.value, pv.name AS variantName
			   |FROM ProductVariantValue pvv
			   |JOIN ProductVariant pv ON pv.id = pvv.variant_id
			   |WHERE pvv.id IN ($placeholders)
			   |ORDER BY pv.displayOrder""".stripMargin
		val stmt = conn.prepareStatement(sql)
		valueIds.zipWithIndex.foreach { case (id, idx) => stmt.setInt(idx + 1, id) }
		val rs = stmt.executeQuery()
		val parts = scala.collection.mutable.ArrayBuffer.empty[String]
		while (rs.next()) {
			parts += s"${rs.getString("variantName")}: ${rs.getString("value")}"
		}
		rs.close()
		stmt.close()
		parts.mkString(", ")
	}
}

/** Variant group bundled with its selectable values for template rendering. */
case class VariantWithValues(variant: ProductVariant, values: Seq[ProductVariantValue])
