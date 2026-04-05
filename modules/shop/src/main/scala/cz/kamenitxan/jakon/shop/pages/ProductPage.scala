package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopProduct
import cz.kamenitxan.jakon.shop.service.ShopProductService

import java.sql.Connection

/**
 * Created by Kamenitxan on 21.12.2025
 */

@CustomPage
class ProductPage extends AbstractCustomPage {

	override protected def generate(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val products = ShopProductService.getAll()
			products.foreach(renderProduct)
		})
	}

	private def loadProducts()(implicit conn: Connection): Seq[ShopProduct] = {
		val sql = "SELECT * FROM ShopProduct WHERE published = true ORDER BY id"
		val stmt = conn.createStatement()
		DBHelper.select(stmt, sql, classOf[ShopProduct]).map(_.entity)
	}

	private def renderProduct(product: ShopProduct)(implicit conn: Connection): Unit = {
		val data = ShopUtils.commonPageData ++ Map(
			"title" -> product.name,
			"product" -> product
		)
		engine.render("productDetail", s"product${product.id}.html", data)
	}
}
