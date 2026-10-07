package cz.kamenitxan.jakon.shop.payments

import cz.kamenitxan.jakon.shop.entity.*
import cz.kamenitxan.jakon.shop.payments.impl.stripe.*
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

class PaymentServiceTest extends AnyFunSuite {

	test("create payment request maps order items fees and customer metadata") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.id = 12
		paymentMethod.name = "Stripe card"
		paymentMethod.description = "Pay by card"
		paymentMethod.gatewayCode = "stripe"

		val shippingMethod = new ShippingMethod()
		shippingMethod.name = "GLS"
		shippingMethod.description = "Delivery next day"

		val customer = new Customer()
		customer.email = "shopper@example.com"

		val order = new ShopOrder()
		order.id = 55
		order.orderNumber = "2026-001"
		order.customer = customer
		order.paymentMethod = paymentMethod
		order.shippingMethod = shippingMethod
		order.shippingPrice = new BigDecimal("89.00")
		order.paymentPrice = new BigDecimal("19.00")

		val orderItem = new ShopOrderItem()
		orderItem.productName = "Coffee beans"
		orderItem.quantity = 2
		orderItem.totalPrice = new BigDecimal("250.00")
		orderItem.note = "Dark roast"

		val request = PaymentService.createPaymentRequest(
			shopOrder = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			shopOrderItems = Seq(orderItem)
		)

		assert(request.currency == "czk")
		assert(request.customerEmail.contains("shopper@example.com"))
		assert(request.metadata("orderId") == "55")
		assert(request.metadata("orderNumber") == "2026-001")
		assert(request.metadata("paymentMethodId") == "12")
		assert(request.lineItems.map(_.name) == Seq("Coffee beans", "GLS", "Stripe card"))
		assert(request.lineItems.head.quantity == 2L)
		assert(request.lineItems.head.unitPrice == new BigDecimal("125.00"))
	}

	test("stripe gateway returns redirect payment initialization") {
		val originalSecretKey = StripePaymentSettings.secretKey
		val originalPublishableKey = StripePaymentSettings.publishableKey

		StripePaymentSettings.secretKey = "sk_test_123"
		StripePaymentSettings.publishableKey = "pk_test_123"

		try {
			var capturedRequest: StripeCheckoutSessionRequest = null
			val stubClient = new StripeCheckoutClient {
				override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = {
					capturedRequest = request
					StripeCheckoutSessionResponse("cs_test_123", "https://checkout.stripe.com/pay/cs_test_123")
				}
				override def fetchSessionStatus(sessionId: String): String = "unpaid"
			}
			val gateway = new StripePaymentGateway(stubClient)

			val order = new ShopOrder()
			order.orderNumber = "2026-002"

			val request = PaymentRequest(
				shopOrder = order,
				lineItems = Seq(PaymentLineItem("Order 2026-002", unitPrice = new BigDecimal("100.00"))),
				successUrl = "https://example.com/payment/success",
				cancelUrl = "https://example.com/payment/cancel",
				currency = "czk",
				metadata = Map("orderNumber" -> "2026-002"),
				customerEmail = Option("shopper@example.com")
			)

			val result = gateway.initializePayment(request)

			assert(capturedRequest != null)
			assert(capturedRequest.apiKey == "sk_test_123")
			assert(capturedRequest.customerEmail.contains("shopper@example.com"))
			assert(result.provider == PaymentGatewayCode.Stripe)
			assert(result.flow == PaymentFlow.Redirect)
			assert(result.externalPaymentId.contains("cs_test_123"))
			assert(result.redirectUrl.contains("https://checkout.stripe.com/pay/cs_test_123"))
			assert(result.publishableKey.contains("pk_test_123"))
		} finally {
			StripePaymentSettings.secretKey = originalSecretKey
			StripePaymentSettings.publishableKey = originalPublishableKey
		}
	}

	test("payment gateway registry resolves stripe gateway by payment method code") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.name = "Card"
		paymentMethod.gatewayCode = " STRIPE "

		val gateway = PaymentGatewayRegistry.resolve(paymentMethod)

		assert(gateway.gatewayCode == PaymentGatewayCode.Stripe)
	}

	test("createPaymentRequest falls back to an order-title line item when there are no items, shipping or fees") {
		val order = new ShopOrder()
		order.id = 77
		order.orderNumber = "2026-003"
		order.totalPrice = new BigDecimal("199.00")

		val request = PaymentService.createPaymentRequest(
			shopOrder = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			shopOrderItems = Seq.empty
		)

		assert(request.lineItems.size == 1)
		assert(request.lineItems.head.name == "Order 2026-003")
		assert(request.lineItems.head.unitPrice == new BigDecimal("199.00"))
	}

	test("createPaymentRequest falls back to a generic title when the order has no order number") {
		val order = new ShopOrder()
		order.orderNumber = null
		order.totalPrice = new BigDecimal("50.00")

		val request = PaymentService.createPaymentRequest(
			shopOrder = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			shopOrderItems = Seq.empty
		)

		assert(request.lineItems.head.name == "Order payment")
	}

	test("createPaymentRequest omits shipping and payment fee line items when their prices are zero") {
		val order = new ShopOrder()
		order.orderNumber = "2026-004"
		order.shippingPrice = BigDecimal.ZERO
		order.paymentPrice = BigDecimal.ZERO

		val orderItem = new ShopOrderItem()
		orderItem.productName = "Tea"
		orderItem.quantity = 1
		orderItem.totalPrice = new BigDecimal("30.00")

		val request = PaymentService.createPaymentRequest(
			shopOrder = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			shopOrderItems = Seq(orderItem)
		)

		assert(request.lineItems.map(_.name) == Seq("Tea"))
	}

	test("createPaymentRequest resolves the unit price from totalPrice divided by quantity when unitPrice is not set") {
		val order = new ShopOrder()
		order.orderNumber = "2026-005"

		val orderItem = new ShopOrderItem()
		orderItem.productName = "Coffee"
		orderItem.quantity = 3
		orderItem.totalPrice = new BigDecimal("30.00")

		val request = PaymentService.createPaymentRequest(
			shopOrder = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			shopOrderItems = Seq(orderItem)
		)

		assert(request.lineItems.head.unitPrice == new BigDecimal("10.00"))
		assert(request.lineItems.head.quantity == 3L)
	}

	test("createPaymentRequest throws when the order is null") {
		assertThrows[IllegalArgumentException] {
			PaymentService.createPaymentRequest(
				shopOrder = null,
				successUrl = "https://example.com/success",
				cancelUrl = "https://example.com/cancel",
				currency = "CZK",
				shopOrderItems = Seq.empty
			)
		}
	}

	test("createPaymentRequest throws when the success or cancel URL is blank") {
		val order = new ShopOrder()
		order.orderNumber = "2026-006"

		assertThrows[IllegalArgumentException] {
			PaymentService.createPaymentRequest(order, "", "https://example.com/cancel", "CZK", Seq.empty)
		}
		assertThrows[IllegalArgumentException] {
			PaymentService.createPaymentRequest(order, "https://example.com/success", "  ", "CZK", Seq.empty)
		}
	}

	test("gatewayRedirectUrl returns None for an order with a manual payment method, without touching the database") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.gatewayCode = "manual"

		val order = new ShopOrder()
		order.orderNumber = "2026-007"
		order.paymentMethod = paymentMethod

		implicit val conn: java.sql.Connection = null

		val redirectUrl = PaymentService.gatewayRedirectUrl(order, "https://example.com/success", "https://example.com/cancel", "CZK")

		assert(redirectUrl.isEmpty)
	}

	test("gatewayRedirectUrl returns None when no payment method is set on the order") {
		val order = new ShopOrder()
		order.orderNumber = "2026-008"
		order.paymentMethod = null

		implicit val conn: java.sql.Connection = null

		val redirectUrl = PaymentService.gatewayRedirectUrl(order, "https://example.com/success", "https://example.com/cancel", "CZK")

		assert(redirectUrl.isEmpty)
	}
}
