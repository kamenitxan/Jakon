package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.ShopOrderItem

import java.sql.Connection

object OrderItemService {

	def getByOrder(orderId: Int)(implicit conn: Connection): Seq[ShopOrderItem] = {
		val sql = "SELECT * FROM ShopOrderItem WHERE order_id = ? ORDER BY id"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, orderId)
		val items = DBHelper.select(stmt, classOf[ShopOrderItem]).map(_.entity)
		stmt.close()
		items
	}
}
