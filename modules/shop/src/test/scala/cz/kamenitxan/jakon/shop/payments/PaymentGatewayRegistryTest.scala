package cz.kamenitxan.jakon.shop.payments

import cz.kamenitxan.jakon.shop.entity.PaymentMethod
import org.scalatest.funsuite.AnyFunSuite

class PaymentGatewayRegistryTest extends AnyFunSuite {

	test("normalizeCode trims and lower-cases the input") {
		assert(PaymentGatewayRegistry.normalizeCode(" STRIPE ") == "stripe")
	}

	test("normalizeCode falls back to manual for null or blank input") {
		assert(PaymentGatewayRegistry.normalizeCode(null) == PaymentGatewayCode.Manual)
		assert(PaymentGatewayRegistry.normalizeCode("") == PaymentGatewayCode.Manual)
		assert(PaymentGatewayRegistry.normalizeCode("   ") == PaymentGatewayCode.Manual)
	}

	test("paymentMethodGatewayCode normalises the payment method's gateway code") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.gatewayCode = " Stripe "
		assert(PaymentGatewayRegistry.paymentMethodGatewayCode(paymentMethod) == "stripe")
	}

	test("paymentMethodGatewayCode throws for a null payment method") {
		assertThrows[IllegalArgumentException] {
			PaymentGatewayRegistry.paymentMethodGatewayCode(null)
		}
	}

	test("resolve returns the manual gateway for a manual payment method") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.gatewayCode = "manual"
		val gateway = PaymentGatewayRegistry.resolve(paymentMethod)
		assert(gateway.gatewayCode == PaymentGatewayCode.Manual)
	}

	test("resolve throws PaymentGatewayException for an unknown gateway code") {
		val paymentMethod = new PaymentMethod()
		paymentMethod.name = "Unknown"
		paymentMethod.gatewayCode = "doesnotexist"
		assertThrows[PaymentGatewayException] {
			PaymentGatewayRegistry.resolve(paymentMethod)
		}
	}

	test("resolveByCode returns None for an unknown code") {
		assert(PaymentGatewayRegistry.resolveByCode("doesnotexist").isEmpty)
	}

	test("resolveByCode is case-insensitive and trims the code") {
		val gateway = PaymentGatewayRegistry.resolveByCode(" STRIPE ")
		assert(gateway.exists(_.gatewayCode == PaymentGatewayCode.Stripe))
	}

	test("gateways map contains both manual and stripe gateways discovered via classpath scanning") {
		assert(PaymentGatewayRegistry.gateways.keySet.contains(PaymentGatewayCode.Manual))
		assert(PaymentGatewayRegistry.gateways.keySet.contains(PaymentGatewayCode.Stripe))
	}
}
