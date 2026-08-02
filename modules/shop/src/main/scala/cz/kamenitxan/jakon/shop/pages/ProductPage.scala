package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopProduct
import cz.kamenitxan.jakon.shop.service.{ProductVariantService, ShopProductService, VariantWithValues}

import java.sql.Connection
import scala.jdk.CollectionConverters.*

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

	private def renderProduct(product: ShopProduct)(implicit conn: Connection): Unit = {
		// category.variants is populated via @ManyToMany when selectDeep loads the product
		val variantWithValues = if (product.category != null) {
			product.category.variants.map { v =>
				VariantWithValues(v, ProductVariantService.loadValuesForVariant(v.id))
			}
		} else {
			Seq.empty
		}
		val data = ShopUtils.commonPageData ++ Map(
			"title" -> product.name,
			"product" -> product,
			"variants" -> variantWithValues.asJava
		)
		engine.render("productDetail", s"product${product.id}.html", data)
	}
}
