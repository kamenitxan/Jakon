package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.ShopOrder
import cz.kamenitxan.jakon.shop.payments.{PaymentFlow, PaymentGatewayCode, PaymentService}
import cz.kamenitxan.jakon.shop.service.OrderItemService
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
						mutable.Map(
							"order" -> order,
							"orderItems" -> orderItems.asJava,
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
					case Some(order) if order.isPaid =>
						ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
					case Some(order) if order.paymentMethod == null || order.paymentMethod.gatewayCode == PaymentGatewayCode.Manual =>
						ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
					case Some(order) =>
						try {
							val baseUrl = s"${ctx.scheme()}://${ctx.host()}"
							val successUrl = s"$baseUrl/order/status?token=$t"
							val cancelUrl = s"$baseUrl/order/status?token=$t"
							val init = PaymentService.initializePayment(order, successUrl, cancelUrl)
							init.flow match {
								case PaymentFlow.Redirect =>
									init.redirectUrl match {
										case Some(url) => ctx.redirect(url, HttpStatus.FOUND)
										case None =>
											Logger.error(s"Payment gateway returned no redirect URL for order ${order.orderNumber}")
											ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
									}
								case _ =>
									ctx.redirect(s"/order/status?token=$t", HttpStatus.FOUND)
							}
						} catch {
							case e: Exception =>
								Logger.error(s"Payment initialization failed for order ${order.orderNumber}: ${e.getMessage}")
								ctx.redirect(s"/order/status?token=$t&paymentError=1", HttpStatus.FOUND)
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
