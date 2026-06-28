package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.service.OrderItemService
import cz.kamenitxan.jakon.shop.{ShopSettings, ShopUtils}
import cz.kamenitxan.jakon.utils.mail.EmailEntity
import io.javalin.http.{Context, HttpStatus}

import java.sql.Connection
import java.time.LocalDateTime
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

@Pagelet(path = "/order/return", authRequired = false)
class OrderReturnPagelet extends AbstractPagelet {

	/**
	 * Displays the return form: item selector, reason dropdown and bank account field.
	 * Requires a valid order {@code token} query parameter.
	 */
	@Get(path = "/form", template = "order/return/form")
	def showForm(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		withOrder(ctx) { order =>
			val orderItems = OrderItemService.getByOrder(order.id)
			val existingReturn = findReturnByOrder(order.id)
			if (existingReturn.isDefined) {
				mutable.Map(
					"error" -> "Pro tuto objednávku již bylo vytvořeno odstoupení od smlouvy.",
					"token" -> tokenFrom(ctx)
				) ++ ShopUtils.commonPageData
			} else {
				mutable.Map(
					"order" -> order,
					"orderItems" -> orderItems.asJava,
					"reasons" -> OrderReturnReason.all.asJava,
					"token" -> tokenFrom(ctx)
				) ++ ShopUtils.commonPageData
			}
		}
	}

	/**
	 * Processes the submitted return form, creates [[cz.kamenitxan.jakon.shop.entity.OrderReturn]] and
	 * [[cz.kamenitxan.jakon.shop.entity.OrderReturnItem]] records, and redirects to the confirmation page.
	 */
	@Post(path = "/create", validate = false)
	def createReturn(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		withOrder(ctx) { order =>
			val token = tokenFrom(ctx)

			if (findReturnByOrder(order.id).isDefined) {
				ctx.redirect(s"/order/status?token=$token", HttpStatus.FOUND)
				return mutable.Map.empty
			}

			val reason = Option(ctx.formParam("reason")).getOrElse("")
			val bankAccount = Option(ctx.formParam("bankAccount")).map(_.trim).getOrElse("")
			val selectedItemIds = Option(ctx.formParamMap().get("itemId"))
				.map(_.asScala.toSeq.flatMap(s => scala.util.Try(s.toInt).toOption))
				.getOrElse(Seq.empty)

			if (selectedItemIds.isEmpty) {
				val orderItems = OrderItemService.getByOrder(order.id)
				return mutable.Map(
					"order" -> order,
					"orderItems" -> orderItems.asJava,
					"reasons" -> OrderReturnReason.all.asJava,
					"token" -> token,
					"formError" -> "Vyberte alespoň jednu položku k vrácení."
				) ++ ShopUtils.commonPageData
			}

			val orderReturn = new OrderReturn
			orderReturn.order = order
			orderReturn.returnNumber = generateReturnNumber
			orderReturn.status = OrderReturnStatus.Pending
			orderReturn.reason = reason
			orderReturn.bankAccount = bankAccount
			orderReturn.createdAt = LocalDateTime.now()
			orderReturn.published = true
			orderReturn.create()

			val orderItems = OrderItemService.getByOrder(order.id)
			val itemMap: Map[Int, ShopOrderItem] = orderItems.map(i => i.id -> i).toMap

			selectedItemIds.distinct.foreach { itemId =>
				itemMap.get(itemId).foreach { item =>
					val returnItem = new OrderReturnItem
					returnItem.orderReturn = orderReturn
					returnItem.orderItem = item
					returnItem.productName = item.productName
					val qty = Option(ctx.formParam(s"qty_$itemId"))
						.flatMap(s => scala.util.Try(s.toInt).toOption)
						.map(q => Math.min(Math.max(q, 1), item.quantity))
						.getOrElse(item.quantity)
					returnItem.quantity = qty
					returnItem.published = true
					returnItem.create()
				}
			}

			Logger.info(s"OrderReturn ${orderReturn.returnNumber} created for order ${order.orderNumber}")
			sendReturnConfirmationEmail(order, orderReturn)
			ctx.redirect(s"/order/return/confirmation?returnNumber=${orderReturn.returnNumber}&token=$token", HttpStatus.FOUND)
			mutable.Map.empty
		}
	}

	/**
	 * Shows the confirmation page with the return address and shipment instructions.
	 */
	@Get(path = "/confirmation", template = "order/return/confirmation")
	def showConfirmation(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		val returnNumber = Option(ctx.queryParam("returnNumber")).getOrElse("")
		val token = tokenFrom(ctx)

		if (returnNumber.isEmpty) {
			return mutable.Map("error" -> "Chybí číslo odstoupení.") ++ ShopUtils.commonPageData
		}

		val orderReturn = findReturnByNumber(returnNumber)
		orderReturn match {
			case None =>
				mutable.Map("error" -> "Odstoupení nebylo nalezeno.") ++ ShopUtils.commonPageData
			case Some(r) =>
				mutable.Map(
					"orderReturn" -> r,
					"token" -> token,
					"returnAddress" -> Map(
						"name"    -> ShopSettings.returnName,
						"street"  -> ShopSettings.returnStreet,
						"city"    -> ShopSettings.returnCity,
						"zip"     -> ShopSettings.returnZip,
						"country" -> ShopSettings.returnCountry
					).asJava
				) ++ ShopUtils.commonPageData
		}
	}

	private def withOrder(ctx: Context)(f: ShopOrder => mutable.Map[String, Any])(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = tokenFrom(ctx)
		if (token.isEmpty) {
			ctx.redirect("/", HttpStatus.FOUND)
			return mutable.Map.empty
		}
		loadOrderByToken(token) match {
			case None =>
				ctx.redirect("/", HttpStatus.FOUND)
				mutable.Map.empty
			case Some(order) =>
				f(order)
		}
	}

	private def tokenFrom(ctx: Context): String = {
		Option(ctx.queryParam("token")).orElse(Option(ctx.formParam("token"))).getOrElse("")
	}

	private def loadOrderByToken(token: String)(implicit conn: Connection): Option[ShopOrder] = {
		implicit val cls: Class[ShopOrder] = classOf[ShopOrder]
		val stmt = conn.prepareStatement("SELECT * FROM ShopOrder WHERE token = ?")
		stmt.setString(1, token)
		DBHelper.selectDeep[ShopOrder](stmt).headOption
	}

	private def findReturnByOrder(orderId: Int)(implicit conn: Connection): Option[OrderReturn] = {
		val stmt = conn.prepareStatement("SELECT * FROM OrderReturn WHERE order_id = ? LIMIT 1")
		stmt.setInt(1, orderId)
		val result = DBHelper.select(stmt, classOf[OrderReturn]).map(_.entity).headOption
		stmt.close()
		result
	}

	private def findReturnByNumber(returnNumber: String)(implicit conn: Connection): Option[OrderReturn] = {
		val stmt = conn.prepareStatement("SELECT * FROM OrderReturn WHERE returnNumber = ? LIMIT 1")
		stmt.setString(1, returnNumber)
		val result = DBHelper.select(stmt, classOf[OrderReturn]).map(_.entity).headOption
		stmt.close()
		result
	}

	private def generateReturnNumber: String = {
		val year = LocalDateTime.now().getYear
		val seq = System.currentTimeMillis() % 1000000
		f"RET-$year-$seq%06d"
	}

	private def sendReturnConfirmationEmail(order: ShopOrder, orderReturn: OrderReturn)(implicit conn: Connection): Unit = {
		val recipientEmail = Option(order.customer).map(_.email).filter(e => e != null && e.nonEmpty)
			.orElse(Option(order.guestEmail).filter(_.nonEmpty))
			.getOrElse("")
		if (recipientEmail.isEmpty) return
		try {
			val email = new EmailEntity(
				"shop/email/returnConfirmation",
				recipientEmail,
				s"Potvrzení odstoupení od smlouvy ${orderReturn.returnNumber}",
				Map(
					"returnNumber"       -> orderReturn.returnNumber,
					"orderNumber"        -> order.orderNumber,
					"orderToken"         -> order.token,
					"billingName"        -> order.billingName,
					"reason"             -> orderReturn.reason,
					"bankAccount"        -> orderReturn.bankAccount,
					"returnAddressName"  -> ShopSettings.returnName,
					"returnAddressStreet"-> ShopSettings.returnStreet,
					"returnAddressCity"  -> ShopSettings.returnCity,
					"returnAddressZip"   -> ShopSettings.returnZip,
					"returnAddressCountry" -> ShopSettings.returnCountry
				)
			)
			email.create()
		} catch {
			case ex: Exception =>
				Logger.error(s"Failed to send return confirmation email for return ${orderReturn.returnNumber}", ex)
		}
	}
}
