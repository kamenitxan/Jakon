package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.service.ShopProductService

import scala.jdk.CollectionConverters.*

/**
 * Created by Kamenitxan on 21.12.2025
 */

@CustomPage
class IndexPage extends AbstractCustomPage {

	override protected def generate(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val products = ShopProductService.getAll()
			val data = ShopUtils.commonPageData ++ Map(
				"title" -> "Naše produkty",
				"products" -> products.asJava
			)
			engine.render("index", "index.html", data)
		})
	}
}
