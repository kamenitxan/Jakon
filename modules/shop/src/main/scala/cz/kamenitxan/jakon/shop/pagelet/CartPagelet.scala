package cz.kamenitxan.jakon.shop.pagelet

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.ShopUtils
import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.service.CartService
import cz.kamenitxan.jakon.utils.mail.EmailEntity
import io.javalin.http.Context

import java.math.BigDecimal
import java.sql.Connection
import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

@Pagelet(path = "/cart")
class CartPagelet extends AbstractPagelet {

	private val CART_COOKIE = "cart_token"
	private val COOKIE_MAX_AGE = 30 * 24 * 3600

	private def getOrSetCartToken(ctx: Context): String = {
		val existing = ctx.cookie(CART_COOKIE)
		if (existing != null && existing.nonEmpty) {
			existing
		} else {
			val token = UUID.randomUUID().toString
			ctx.cookie(CART_COOKIE, token, COOKIE_MAX_AGE)
			token
		}
	}

	@Get(path = "", template = "cart/step1")
	def showCart(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val cart = CartService.getOrCreateCart(token)
		val items = CartService.getItems(cart.id)
		val total = CartService.getTotalPrice(items)
		mutable.Map(
			"cart" -> cart,
			"cartItems" -> items.asJava,
			"total" -> total,
			"step" -> 1
		) ++ ShopUtils.commonPageData
	}

	class AddData {
		var productId: String = ""
		var quantity: String = "1"
	}

	@Post(path = "/add")
	def addToCart(ctx: Context, data: AddData)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val productIdOpt = Option(data.productId).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption)
		val qty = Option(data.quantity).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption).getOrElse(1)
		productIdOpt match {
			case Some(productId) =>
				val cart = CartService.getOrCreateCart(token)
				CartService.addItem(cart.id, productId, qty)
				Logger.debug(s"Added product $productId qty=$qty to cart ${cart.id}")
			case None =>
				Logger.warn("addToCart called without valid productId")
		}
		redirect(ctx, "/cart")
		mutable.Map.empty
	}

	class UpdateData {
		var itemId: String = ""
		var quantity: String = "0"
	}

	@Post(path = "/update")
	def updateCart(ctx: Context, data: UpdateData)(implicit conn: Connection): mutable.Map[String, Any] = {
		val itemIdOpt = Option(data.itemId).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption)
		val qty = Option(data.quantity).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption).getOrElse(0)
		itemIdOpt.foreach(id => CartService.updateItemQuantity(id, qty))
		redirect(ctx, "/cart")
		mutable.Map.empty
	}

	@Get(path = "/checkout", template = "cart/step2")
	def showCheckout(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val cart = CartService.getOrCreateCart(token)
		val items = CartService.getItems(cart.id)
		if (items.isEmpty) {
			redirect(ctx, "/cart")
			return mutable.Map.empty
		}
		val shippingMethods = loadShippingMethods
		val paymentMethods = loadPaymentMethods
		val total = CartService.getTotalPrice(items)
		mutable.Map(
			"cart" -> cart,
			"cartItems" -> items.asJava,
			"total" -> total,
			"shippingMethods" -> shippingMethods.asJava,
			"paymentMethods" -> paymentMethods.asJava,
			"step" -> 2
		) ++ ShopUtils.commonPageData
	}

	class CheckoutData {
		var shippingMethodId: String = ""
		var paymentMethodId: String = ""
	}

	@Post(path = "/checkout")
	def saveCheckout(ctx: Context, data: CheckoutData)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val cart = CartService.getOrCreateCart(token)
		val shippingIdOpt = Option(data.shippingMethodId).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption)
		val paymentIdOpt = Option(data.paymentMethodId).filter(_.nonEmpty).flatMap(s => Try(s.toInt).toOption)
		(shippingIdOpt, paymentIdOpt) match {
			case (Some(sid), Some(pid)) =>
				CartService.saveShippingAndPayment(cart.id, sid, pid)
				redirect(ctx, "/cart/delivery")
			case _ =>
				Logger.warn("saveCheckout called without shipping or payment selection")
				redirect(ctx, "/cart/checkout")
		}
		mutable.Map.empty
	}

	@Get(path = "/delivery", template = "cart/step3")
	def showDelivery(ctx: Context)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val cart = CartService.getOrCreateCart(token)
		val items = CartService.getItems(cart.id)
		if (items.isEmpty || cart.selectedShippingMethod == null || cart.selectedPaymentMethod == null) {
			redirect(ctx, "/cart")
			return mutable.Map.empty
		}
		val total = CartService.getTotalPrice(items)
		val grandTotal = total
			.add(Option(cart.selectedShippingMethod).map(_.price).getOrElse(BigDecimal.ZERO))
			.add(Option(cart.selectedPaymentMethod).map(_.price).getOrElse(BigDecimal.ZERO))

		val prefill = prefillFromUser(ctx)

		mutable.Map(
			"cart" -> cart,
			"cartItems" -> items.asJava,
			"itemsTotal" -> total,
			"grandTotal" -> grandTotal,
			"step" -> 3
		) ++ prefill ++ ShopUtils.commonPageData
	}

	class DeliveryData {
		var guestEmail: String = ""
		var guestPhone: String = ""
		var billingName: String = ""
		var billingStreet: String = ""
		var billingCity: String = ""
		var billingZip: String = ""
		var billingCountry: String = ""
		var deliveryName: String = ""
		var deliveryStreet: String = ""
		var deliveryCity: String = ""
		var deliveryZip: String = ""
		var deliveryCountry: String = ""
		var customerNote: String = ""
	}

	@Post(path = "/submit", validate = false)
	def submitOrder(ctx: Context, data: DeliveryData)(implicit conn: Connection): mutable.Map[String, Any] = {
		val token = getOrSetCartToken(ctx)
		val cart = CartService.getOrCreateCart(token)
		val items = CartService.getItems(cart.id)

		if (items.isEmpty) {
			redirect(ctx, "/cart")
			return mutable.Map.empty
		}
		if (cart.selectedShippingMethod == null || cart.selectedPaymentMethod == null) {
			redirect(ctx, "/cart/checkout")
			return mutable.Map.empty
		}
		if (Option(data.guestEmail).forall(_.trim.isEmpty) || Option(data.billingName).forall(_.trim.isEmpty)) {
			redirect(ctx, "/cart/delivery")
			return mutable.Map.empty
		}

		val itemsTotal = CartService.getTotalPrice(items)
		val shippingPrice = Option(cart.selectedShippingMethod).map(_.price).getOrElse(BigDecimal.ZERO)
		val paymentPrice = Option(cart.selectedPaymentMethod).map(_.price).getOrElse(BigDecimal.ZERO)
		val grandTotal = itemsTotal.add(shippingPrice).add(paymentPrice)

		val orderNumber = generateOrderNumber
		val orderToken = UUID.randomUUID().toString

		val order = new Order
		order.orderNumber = orderNumber
		order.token = orderToken
		order.orderDate = LocalDateTime.now()
		order.status = "NEW"
		order.totalPrice = grandTotal
		order.shippingPrice = shippingPrice
		order.paymentPrice = paymentPrice
		order.shippingMethod = cart.selectedShippingMethod
		order.paymentMethod = cart.selectedPaymentMethod
		order.guestEmail = data.guestEmail.trim
		order.guestPhone = Option(data.guestPhone).map(_.trim).getOrElse("")
		order.billingName = Option(data.billingName).map(_.trim).getOrElse("")
		order.billingStreet = Option(data.billingStreet).map(_.trim).getOrElse("")
		order.billingCity = Option(data.billingCity).map(_.trim).getOrElse("")
		order.billingZip = Option(data.billingZip).map(_.trim).getOrElse("")
		order.billingCountry = Option(data.billingCountry).map(_.trim).getOrElse("")
		order.deliveryName = Option(data.deliveryName).map(_.trim).getOrElse(order.billingName)
		order.deliveryStreet = Option(data.deliveryStreet).map(_.trim).getOrElse(order.billingStreet)
		order.deliveryCity = Option(data.deliveryCity).map(_.trim).getOrElse(order.billingCity)
		order.deliveryZip = Option(data.deliveryZip).map(_.trim).getOrElse(order.billingZip)
		order.deliveryCountry = Option(data.deliveryCountry).map(_.trim).getOrElse(order.billingCountry)
		order.customerNote = Option(data.customerNote).map(_.trim).getOrElse("")
		order.published = true

		val user: JakonUser = ctx.sessionAttribute("user")
		if (user != null && user.isInstanceOf[Customer]) {
			order.customer = user.asInstanceOf[Customer]
		}

		order.create()

		items.foreach { item =>
			val orderItem = new OrderItem
			orderItem.order = order
			orderItem.product = item.product
			orderItem.productName = if (item.product != null) item.product.name else ""
			orderItem.quantity = item.quantity
			val unitPrice = if (item.product != null && item.product.discountPrice != null) item.product.discountPrice
			               else if (item.product != null) item.product.price
			               else BigDecimal.ZERO
			orderItem.unitPrice = unitPrice
			orderItem.totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.quantity))
			orderItem.published = true
			orderItem.create()
		}

		CartService.deleteCart(cart.id)
		ctx.cookie(CART_COOKIE, "", 0)

		sendConfirmationEmail(order, items)

		redirect(ctx, s"/cart/success?orderNumber=${order.orderNumber}")
		mutable.Map.empty
	}

	@Get(path = "/success", template = "cart/success")
	def showSuccess(ctx: Context): mutable.Map[String, Any] = {
		val orderNumber = Option(ctx.queryParam("orderNumber")).getOrElse("")
		mutable.Map(
			"orderNumber" -> orderNumber
		)
	}

	private def generateOrderNumber: String = {
		val year = LocalDateTime.now().getYear
		val seq = System.currentTimeMillis() % 1000000
		f"ORD-$year-$seq%06d"
	}

	private def prefillFromUser(ctx: Context): mutable.Map[String, Any] = {
		val user: JakonUser = ctx.sessionAttribute("user")
		if (user == null) {
			mutable.Map.empty
		} else if (user.isInstanceOf[Customer]) {
			val c = user.asInstanceOf[Customer]
			mutable.Map(
				"prefillEmail" -> c.email,
				"prefillPhone" -> c.phone,
				"prefillName" -> s"${c.firstName} ${c.lastName}".trim,
				"prefillStreet" -> c.street,
				"prefillCity" -> c.city,
				"prefillZip" -> c.zip,
				"prefillCountry" -> c.country,
				"prefillDeliveryStreet" -> c.deliveryStreet,
				"prefillDeliveryCity" -> c.deliveryCity,
				"prefillDeliveryZip" -> c.deliveryZip,
				"prefillDeliveryCountry" -> c.deliveryCountry
			)
		} else {
			mutable.Map(
				"prefillEmail" -> user.email,
				"prefillName" -> s"${user.firstName} ${user.lastName}".trim
			)
		}
	}

	private def loadShippingMethods(implicit conn: Connection): Seq[ShippingMethod] = {
		val sql = "SELECT * FROM ShippingMethod JOIN JakonObject ON JakonObject.id = ShippingMethod.id WHERE ShippingMethod.enabled = true ORDER BY ShippingMethod.displayOrder"
		implicit val cls: Class[ShippingMethod] = classOf[ShippingMethod]
		val stmt = conn.createStatement()
		DBHelper.selectDeep[ShippingMethod](stmt, sql)
	}

	private def loadPaymentMethods(implicit conn: Connection): Seq[PaymentMethod] = {
		val sql = "SELECT * FROM PaymentMethod JOIN JakonObject ON JakonObject.id = PaymentMethod.id WHERE PaymentMethod.enabled = true ORDER BY PaymentMethod.displayOrder"
		implicit val cls: Class[PaymentMethod] = classOf[PaymentMethod]
		val stmt = conn.createStatement()
		DBHelper.selectDeep[PaymentMethod](stmt, sql)
	}

	private def sendConfirmationEmail(order: Order, items: Seq[CartItem])(implicit conn: Connection): Unit = {
		try {
			val email = new EmailEntity(
				"shop/email/orderConfirmation",
				order.guestEmail,
				s"Potvrzení objednávky ${order.orderNumber}",
				Map(
					"orderNumber" -> order.orderNumber,
					"orderToken" -> order.token,
					"billingName" -> order.billingName,
					"totalPrice" -> order.totalPrice.toString,
					"shippingMethod" -> Option(order.shippingMethod).map(_.name).getOrElse(""),
					"paymentMethod" -> Option(order.paymentMethod).map(_.name).getOrElse("")
				)
			)
			email.create()
		} catch {
			case ex: Exception => Logger.error(s"Failed to create confirmation email for order ${order.orderNumber}", ex)
		}
	}
}
