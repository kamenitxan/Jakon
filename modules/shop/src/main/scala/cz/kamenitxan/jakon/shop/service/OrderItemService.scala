package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.OrderItem

import java.sql.Connection

object OrderItemService {

	def getByOrder(orderId: Int)(implicit conn: Connection): Seq[OrderItem] = {
		// Direct query without JakonObject JOIN — OrderItem is standAlone
		val sql = "SELECT * FROM OrderItem WHERE order_id = ? ORDER BY id"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, orderId)
		val items = DBHelper.select(stmt, classOf[OrderItem]).map(_.entity)
		stmt.close()
		items
	}
}
