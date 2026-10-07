package cz.kamenitxan.jakon.shop.payments.impl.stripe

import cz.kamenitxan.jakon.shop.entity.ShopOrder
import cz.kamenitxan.jakon.shop.payments.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

class StripePaymentGatewayTest extends AnyFunSuite with BeforeAndAfterEach {

	private var originalSecretKey: String = _
	private var originalPublishableKey: String = _

	override def beforeEach(): Unit = {
		originalSecretKey = StripePaymentSettings.secretKey
		originalPublishableKey = StripePaymentSettings.publishableKey
	}

	override def afterEach(): Unit = {
		StripePaymentSettings.secretKey = originalSecretKey
		StripePaymentSettings.publishableKey = originalPublishableKey
	}

	private def sampleRequest(lineItems: Seq[PaymentLineItem] = Seq(PaymentLineItem("Item", unitPrice = new BigDecimal("10.00")))): PaymentRequest = {
		val order = new ShopOrder()
		order.orderNumber = "2026-020"
		PaymentRequest(
			shopOrder = order,
			lineItems = lineItems,
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "czk",
			metadata = Map.empty,
			customerEmail = None
		)
	}

	private val neverCalledClient: StripeCheckoutClient = new StripeCheckoutClient {
		override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse =
			fail("createSession should not have been called")
		override def fetchSessionStatus(sessionId: String): String =
			fail("fetchSessionStatus should not have been called")
	}

	test("gatewayCode is stripe") {
		assert(new StripePaymentGateway(neverCalledClient).gatewayCode == PaymentGatewayCode.Stripe)
	}

	test("isConfigured reflects StripePaymentSettings") {
		StripePaymentSettings.secretKey = null
		StripePaymentSettings.publishableKey = null
		assert(!new StripePaymentGateway(neverCalledClient).isConfigured)

		StripePaymentSettings.secretKey = "sk_test_1"
		StripePaymentSettings.publishableKey = "pk_test_1"
		assert(new StripePaymentGateway(neverCalledClient).isConfigured)
	}

	test("initializePayment throws when the gateway is not configured") {
		StripePaymentSettings.secretKey = null
		StripePaymentSettings.publishableKey = null

		val gateway = new StripePaymentGateway(neverCalledClient)
		assertThrows[PaymentGatewayException] {
			gateway.initializePayment(sampleRequest())
		}
	}

	test("initializePayment throws when there are no line items") {
		StripePaymentSettings.secretKey = "sk_test_1"
		StripePaymentSettings.publishableKey = "pk_test_1"

		val gateway = new StripePaymentGateway(neverCalledClient)
		assertThrows[PaymentGatewayException] {
			gateway.initializePayment(sampleRequest(lineItems = Seq.empty))
		}
	}

	test("fetchPaymentStatus maps 'paid' to Completed") {
		val client = new StripeCheckoutClient {
			override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = ???
			override def fetchSessionStatus(sessionId: String): String = "paid"
		}
		val gateway = new StripePaymentGateway(client)
		assert(gateway.fetchPaymentStatus("cs_test_1").contains(PaymentStatus.Completed))
	}

	test("fetchPaymentStatus maps 'no_payment_required' to Completed") {
		val client = new StripeCheckoutClient {
			override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = ???
			override def fetchSessionStatus(sessionId: String): String = "no_payment_required"
		}
		val gateway = new StripePaymentGateway(client)
		assert(gateway.fetchPaymentStatus("cs_test_1").contains(PaymentStatus.Completed))
	}

	test("fetchPaymentStatus maps 'unpaid' and unknown statuses to Pending") {
		val client = new StripeCheckoutClient {
			override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = ???
			override def fetchSessionStatus(sessionId: String): String = "unpaid"
		}
		val gateway = new StripePaymentGateway(client)
		assert(gateway.fetchPaymentStatus("cs_test_1").contains(PaymentStatus.Pending))

		val unknownClient = new StripeCheckoutClient {
			override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = ???
			override def fetchSessionStatus(sessionId: String): String = "something_else"
		}
		assert(new StripePaymentGateway(unknownClient).fetchPaymentStatus("cs_test_1").contains(PaymentStatus.Pending))
	}

	test("fetchPaymentStatus returns None for null or blank payment ids") {
		val gateway = new StripePaymentGateway(neverCalledClient)
		assert(gateway.fetchPaymentStatus(null).isEmpty)
		assert(gateway.fetchPaymentStatus("").isEmpty)
		assert(gateway.fetchPaymentStatus("   ").isEmpty)
	}
}
