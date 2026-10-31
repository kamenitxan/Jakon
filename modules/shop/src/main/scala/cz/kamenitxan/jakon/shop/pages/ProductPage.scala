package cz.kamenitxan.jakon.shop.pages

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopProduct
import cz.kamenitxan.jakon.shop.service.{ProductVariantService, ShopProductService}

import java.sql.Connection

/**
 * Created by Kamenitxan on 21.12.2025
 *
 * Renders detail of every product. Variant of a product is a standalone product, so it has its own page
 * with its values preselected in variant selector.
 */
@CustomPage
class ProductPage extends AbstractCustomPage {

	private val gson = new Gson

	override protected def generate(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val products = ShopProductService.getAll()
			products.foreach(renderProduct)
		})
	}

	private def renderProduct(product: ShopProduct)(implicit conn: Connection): Unit = {
		val group = ProductVariantService.loadVariantGroup(product)
		val selector = group.find(_.id == product.id)
			.flatMap(p => ProductVariantService.buildSelector(p, group))
		val data = ShopUtils.commonPageData ++ Map(
			"title" -> product.name,
			"product" -> product,
			"variantSelector" -> selector.orNull,
			"variantOptionsJson" -> selector.map(s => gson.toJson(s.options)).getOrElse("[]")
		)
		engine.render("productDetail", s"product${product.id}.html", data)
	}
}
