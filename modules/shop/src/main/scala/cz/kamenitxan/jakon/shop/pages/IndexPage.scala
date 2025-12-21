package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}

/**
 * Created by Kamenitxan on 21.12.2025
 */

@CustomPage
class IndexPage extends AbstractCustomPage {

	override protected def generate(): Unit = {
		engine.render("index", "index.html", Map(
			"title" -> "Index"
		))
	}
}
