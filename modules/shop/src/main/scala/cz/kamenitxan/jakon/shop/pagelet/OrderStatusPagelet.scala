package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}
import cz.kamenitxan.jakon.shop.entity.ShopOrder
import cz.kamenitxan.jakon.shop.service.OrderItemService
import io.javalin.http.Context

import java.sql.Connection
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

@Pagelet(path = "/order", authRequired = false)
class OrderStatusPagelet extends AbstractPagelet {

	@Get(path = "/status", template = "order/status")
	def showOrderStatus(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = Option(ctx.queryParam("token")).filter(_.nonEmpty)
		token match {
			case None =>
				mutable.Map("error" -> "Chybí token objednávky.")
			case Some(t) =>
				loadOrderByToken(t) match {
					case None =>
						mutable.Map("error" -> "Objednávka nenalezena nebo neplatný odkaz.")
					case Some(order) =>
						val orderItems = OrderItemService.getByOrder(order.id)
						mutable.Map(
							"order" -> order,
							"orderItems" -> orderItems.asJava
						)
				}
		}
	}

	private def loadOrderByToken(token: String)(implicit conn: Connection): Option[ShopOrder] = {
		implicit val cls: Class[ShopOrder] = classOf[ShopOrder]
		val sql = "SELECT * FROM `Order` WHERE token = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, token)
		DBHelper.selectDeep[ShopOrder](stmt).headOption
	}
}
