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

		val order = new Order()
		order.id = 55
		order.orderNumber = "2026-001"
		order.customer = customer
		order.paymentMethod = paymentMethod
		order.shippingMethod = shippingMethod
		order.shippingPrice = new BigDecimal("89.00")
		order.paymentPrice = new BigDecimal("19.00")

		val orderItem = new OrderItem()
		orderItem.productName = "Coffee beans"
		orderItem.quantity = 2
		orderItem.totalPrice = new BigDecimal("250.00")
		orderItem.note = "Dark roast"

		val request = PaymentService.createPaymentRequest(
			order = order,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "CZK",
			orderItems = Seq(orderItem)
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
			val gateway = new StripePaymentGateway((request: StripeCheckoutSessionRequest) => {
				capturedRequest = request
				StripeCheckoutSessionResponse("cs_test_123", "https://checkout.stripe.com/pay/cs_test_123")
			})

			val order = new Order()
			order.orderNumber = "2026-002"

			val request = PaymentRequest(
				order = order,
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
}
