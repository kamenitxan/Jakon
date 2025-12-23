package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.shop.ShopUtils

/**
 * Created by Kamenitxan on 21.12.2025
 */

@CustomPage
class IndexPage extends AbstractCustomPage {

	override protected def generate(): Unit = {
		val data = ShopUtils.commonPageData ++ Map(
			"title" -> "Index"
		)
		engine.render("index", "index.html", data)
	}
}
