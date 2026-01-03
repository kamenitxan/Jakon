package cz.kamenitxan.jakon.shop.pages

import cz.kamenitxan.jakon.core.custom_pages.{AbstractCustomPage, CustomPage}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopCategory
import cz.kamenitxan.jakon.shop.service.ShopCategoryService

import java.sql.Connection

/**
 * Created by Kamenitxan on 23.12.2025
 */
@CustomPage
class CategoryListPage extends AbstractCustomPage {

	override protected def generate(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val categories = ShopCategoryService.getAll()
			categories.foreach(renderCategory)
		})
	}

	private def renderCategory(category: ShopCategory)(implicit conn: Connection): Unit = {
		val data = ShopUtils.commonPageData ++ Map(
			"title" -> category.name,
			"category" -> category
		)
		engine.render("components/category", category.createUrl, data)
	}
}
