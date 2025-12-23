package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.ShopCategory

import java.sql.Connection

/**
 * Created by Kamenitxan on 22.12.2025
 */
object ShopCategoryService {

	implicit val cls: Class[ShopCategory] = classOf[ShopCategory]

	def getAll()(implicit conn: Connection): Seq[ShopCategory] = {
		val sql = "SELECT * FROM ShopCategory LEFT JOIN ShopCategory p ON ShopCategory.parentCategory_id = p.id ORDER BY p.id;"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}

}
