package cz.kamenitxan.jakon.shop

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.ShopCategory
import cz.kamenitxan.jakon.shop.service.ShopCategoryService

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

/**
 * Created by Kamenitxan on 21.12.2025
 */
object ShopUtils {

	private def categories() = ShopCategoryService.getAll()(DBHelper.getConnection)

	def categoryTree(): Seq[ShopCategory] = {
		val roots = categories().filter(_.parentCategory == null)
		roots
	}

	val commonPageData: Map[String, AnyRef] = {
		Map(
			"categories" -> categoryTree().asJava,
			"currentDate" -> LocalDate.now
		)
	}

}
