package cz.kamenitxan.jakon.shop.payments

import cz.kamenitxan.jakon.shop.entity.{Order, OrderItem}
import cz.kamenitxan.jakon.shop.payments.impl.stripe.StripePaymentSettings
import cz.kamenitxan.jakon.shop.service.OrderItemService

import java.math.{BigDecimal, RoundingMode}
import java.sql.Connection

/**
 * Application-level service that orchestrates payment initialisation.
 *
 * Resolves the appropriate [[PaymentGateway]] for an order's payment method,
 * builds a [[PaymentRequest]] from the order and its items, and delegates to the gateway.
 * The caller receives a [[PaymentInitialization]] describing how to proceed
 * (e.g. redirect URL for hosted checkout, manual instructions, etc.).
 */
object PaymentService {

	/**
	 * Resolves the gateway for the order's payment method, builds a payment request and
	 * initiates the payment session.
	 *
	 * @param order      the order to pay; must not be {@code null} and must have a payment method set
	 * @param successUrl URL the customer is redirected to after a successful payment
	 * @param cancelUrl  URL the customer is redirected to when the payment is cancelled
	 * @param currency   ISO 4217 currency code; defaults to the value from [[StripePaymentSettings]]
	 * @param conn       implicit database connection used to load order items
	 * @return [[PaymentInitialization]] describing how to proceed with the payment
	 * @throws PaymentGatewayException if the gateway is not configured or the request is invalid
	 */
	def initializePayment(order: Order, successUrl: String, cancelUrl: String, currency: String = StripePaymentSettings.currency)(implicit conn: Connection): PaymentInitialization = {
		val request = createPaymentRequest(order, successUrl, cancelUrl, currency)
		val gateway = PaymentGatewayRegistry.resolve(order.paymentMethod)
		if (!gateway.isConfigured) {
			throw new PaymentGatewayException(s"Payment gateway '${gateway.gatewayCode}' is not configured.")
		}
		gateway.initializePayment(request)
	}

	/**
	 * Builds a [[PaymentRequest]] for the given order, loading its items from the database.
	 *
	 * @param order      the order to build the request for
	 * @param successUrl URL the customer is redirected to after a successful payment
	 * @param cancelUrl  URL the customer is redirected to when the payment is cancelled
	 * @param currency   ISO 4217 currency code; defaults to the value from [[StripePaymentSettings]]
	 * @param conn       implicit database connection used to load order items
	 * @return the assembled [[PaymentRequest]]
	 */
	def createPaymentRequest(order: Order, successUrl: String, cancelUrl: String, currency: String = StripePaymentSettings.currency)(implicit conn: Connection): PaymentRequest = {
		val orderItems = if (order != null && order.id > 0) {
			OrderItemService.getByOrder(order.id)
		} else {
			Seq.empty
		}
		createPaymentRequest(order, successUrl, cancelUrl, currency, orderItems)
	}

	/**
	 * Builds a [[PaymentRequest]] from the given order and pre-loaded items.
	 * Used internally and in tests to avoid a database call.
	 */
	private[payments] def createPaymentRequest(order: Order, successUrl: String, cancelUrl: String, currency: String, orderItems: Seq[OrderItem]): PaymentRequest = {
		require(order != null, "Order must not be null.")
		require(Option(successUrl).exists(_.trim.nonEmpty), "Success URL must not be blank.")
		require(Option(cancelUrl).exists(_.trim.nonEmpty), "Cancel URL must not be blank.")

		val lineItems = mapOrderItems(order, orderItems) ++ shippingLineItem(order) ++ paymentFeeLineItem(order)
		val fallbackLineItems = if (lineItems.nonEmpty) {
			lineItems
		} else {
			Seq(
				PaymentLineItem(
					name = orderTitle(order),
					unitPrice = safeAmount(order.totalPrice)
				)
			)
		}

		PaymentRequest(
			order = order,
			lineItems = fallbackLineItems,
			successUrl = successUrl,
			cancelUrl = cancelUrl,
			currency = currency.toLowerCase,
			metadata = metadata(order),
			customerEmail = Option(order.customer).map(_.email).filter(value => value != null && value.trim.nonEmpty)
		)
	}

	/** Maps order items to [[PaymentLineItem]] instances. */
	private def mapOrderItems(order: Order, orderItems: Seq[OrderItem]): Seq[PaymentLineItem] = {
		orderItems.map(item => PaymentLineItem(
			name = Option(item.productName).filter(_.trim.nonEmpty).getOrElse(orderTitle(order)),
			quantity = Math.max(item.quantity, 1).toLong,
			unitPrice = resolveUnitPrice(item),
			description = Option(item.note).filter(_.trim.nonEmpty)
		))
	}

	/** Returns a shipping line item if the order has a positive shipping price. */
	private def shippingLineItem(order: Order): Seq[PaymentLineItem] = {
		if (isPositive(order.shippingPrice)) {
			Seq(PaymentLineItem(
				name = Option(order.shippingMethod).map(_.name).filter(_.trim.nonEmpty).getOrElse("Shipping"),
				unitPrice = order.shippingPrice,
				description = Option(order.shippingMethod).map(_.description).filter(_.trim.nonEmpty)
			))
		} else {
			Seq.empty
		}
	}

	/** Returns a payment-fee line item if the order has a positive payment price. */
	private def paymentFeeLineItem(order: Order): Seq[PaymentLineItem] = {
		if (isPositive(order.paymentPrice)) {
			Seq(PaymentLineItem(
				name = Option(order.paymentMethod).map(_.name).filter(_.trim.nonEmpty).getOrElse("Payment"),
				unitPrice = order.paymentPrice,
				description = Option(order.paymentMethod).map(_.description).filter(_.trim.nonEmpty)
			))
		} else {
			Seq.empty
		}
	}

	/** Builds the metadata map forwarded to the payment gateway for reconciliation. */
	private def metadata(order: Order): Map[String, String] = {
		Map(
			"orderId" -> order.id.toString,
			"orderNumber" -> Option(order.orderNumber).getOrElse(""),
			"paymentMethodId" -> Option(order.paymentMethod).map(_.id.toString).getOrElse("")
		).filter(_._2.nonEmpty)
	}

	private def orderTitle(order: Order): String = {
		Option(order.orderNumber).filter(_.trim.nonEmpty).map(number => s"Order $number").getOrElse("Order payment")
	}

	private def safeAmount(amount: BigDecimal): BigDecimal = {
		Option(amount).getOrElse(BigDecimal.ZERO)
	}

	private def isPositive(amount: BigDecimal): Boolean = {
		amount != null && amount.compareTo(BigDecimal.ZERO) > 0
	}

	/**
	 * Resolves the unit price for an order item.
	 * Prefers explicit unit price; falls back to dividing total price by quantity.
	 */
	private def resolveUnitPrice(item: OrderItem): BigDecimal = {
		if (isPositive(item.unitPrice)) {
			item.unitPrice
		} else if (isPositive(item.totalPrice)) {
			safeAmount(item.totalPrice).divide(BigDecimal.valueOf(Math.max(item.quantity, 1).toLong), 2, RoundingMode.HALF_UP)
		} else {
			BigDecimal.ZERO
		}
	}
}
