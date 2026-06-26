package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopOrder
import cz.kamenitxan.jakon.shop.payments.{PaymentGatewayCode, PaymentService, PaymentStatus}
import cz.kamenitxan.jakon.shop.service.{OrderItemService, OrderPaymentService}
import io.javalin.http.{Context, HttpStatus}

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
						mutable.Map("error" -> "Objednávka nenalezena nebo neplatný odkaz.") ++ ShopUtils.commonPageData
					case Some(order) =>
						val orderItems = OrderItemService.getByOrder(order.id)
							val isPaid = OrderPaymentService.getByOrder(order.id).exists(_.status == PaymentStatus.Completed)
							mutable.Map(
								"order" -> order,
								"orderItems" -> orderItems.asJava,
								"isPaid" -> isPaid,
								"paymentError" -> Option(ctx.queryParam("paymentError")).getOrElse("")
							) ++ ShopUtils.commonPageData
				}
		}
	}

	@Get(path = "/pay")
	def initiatePayment(ctx: Context)(implicit conn: Connection): Unit = {
		val token = Option(ctx.queryParam("token")).filter(_.nonEmpty)
		token match {
			case None =>
				ctx.redirect("/", HttpStatus.FOUND)
			case Some(t) =>
				loadOrderByToken(t) match {
					case None =>
						ctx.redirect("/", HttpStatus.FOUND)
					case Some(order) if OrderPaymentService.getByOrder(order.id).exists(_.status == PaymentStatus.Completed) =>
						ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
					case Some(order) if order.paymentMethod == null || order.paymentMethod.gatewayCode == PaymentGatewayCode.Manual =>
						ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
					case Some(order) =>
						val baseUrl = s"${ctx.scheme()}://${ctx.host()}"
						val successUrl = s"$baseUrl/order/status?token=$t"
						val cancelUrl = s"$baseUrl/order/status?token=$t&paymentError=1"
						PaymentService.gatewayRedirectUrl(order, successUrl, cancelUrl) match {
							case Some(url) => ctx.redirect(url, HttpStatus.FOUND)
							case None => ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
						}
				}
		}
	}

	private def loadOrderByToken(token: String)(implicit conn: Connection): Option[ShopOrder] = {
		implicit val cls: Class[ShopOrder] = classOf[ShopOrder]
		val sql = "SELECT * FROM ShopOrder WHERE token = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, token)
		DBHelper.selectDeep[ShopOrder](stmt).headOption
	}
}
