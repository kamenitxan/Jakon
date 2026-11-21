package cz.kamenitxan.jakon.shop.payments.impl.manual

import cz.kamenitxan.jakon.shop.entity.ShopOrder
import cz.kamenitxan.jakon.shop.payments.*
import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

class ManualPaymentGatewayTest extends AnyFunSuite {

	test("gatewayCode is manual and the gateway is always configured") {
		assert(ManualPaymentGateway.gatewayCode == PaymentGatewayCode.Manual)
		assert(ManualPaymentGateway.isConfigured)
	}

	test("initializePayment returns a manual flow result echoing the request metadata") {
		val order = new ShopOrder()
		order.orderNumber = "2026-010"

		val request = PaymentRequest(
			shopOrder = order,
			lineItems = Seq(PaymentLineItem("Order 2026-010", unitPrice = new BigDecimal("50.00"))),
			successUrl = "https://example.com/success",
			cancelUrl = "https://example.com/cancel",
			currency = "czk",
			metadata = Map("orderNumber" -> "2026-010"),
			customerEmail = None
		)

		val result = ManualPaymentGateway.initializePayment(request)

		assert(result.provider == PaymentGatewayCode.Manual)
		assert(result.flow == PaymentFlow.Manual)
		assert(result.externalPaymentId.isEmpty)
		assert(result.redirectUrl.isEmpty)
		assert(result.metadata == Map("orderNumber" -> "2026-010"))
	}

	test("fetchPaymentStatus is not supported by the manual gateway") {
		assert(ManualPaymentGateway.fetchPaymentStatus("anything").isEmpty)
	}
}
