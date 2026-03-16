package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.OrderItem

import java.sql.Connection

object OrderItemService {

	implicit val cls: Class[OrderItem] = classOf[OrderItem]

	def getByOrder(orderId: Int)(implicit conn: Connection): Seq[OrderItem] = {
		val sql = "SELECT * FROM OrderItem JOIN JakonObject ON JakonObject.id = OrderItem.id WHERE order_id = ? ORDER BY OrderItem.id"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, orderId)
		DBHelper.selectDeep(stmt)
	}
}
