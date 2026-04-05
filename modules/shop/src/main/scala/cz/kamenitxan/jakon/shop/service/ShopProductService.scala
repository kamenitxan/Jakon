package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.ShopProduct

import java.sql.Connection

object ShopProductService {

	implicit val cls: Class[ShopProduct] = classOf[ShopProduct]

	def getAll()(implicit conn: Connection): Seq[ShopProduct] = {
		val sql = "SELECT * FROM ShopProduct JOIN JakonObject ON ShopProduct.id = JakonObject.id WHERE JakonObject.published = true ORDER BY id"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}
}
