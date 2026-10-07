package cz.kamenitxan.jakon.shop.payments.impl.stripe

import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class StripePaymentSettingsTest extends AnyFunSuite with BeforeAndAfterEach {

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

	test("isConfigured is false when either key is missing") {
		StripePaymentSettings.secretKey = null
		StripePaymentSettings.publishableKey = null
		assert(!StripePaymentSettings.isConfigured)

		StripePaymentSettings.secretKey = "sk_test_1"
		StripePaymentSettings.publishableKey = null
		assert(!StripePaymentSettings.isConfigured)

		StripePaymentSettings.secretKey = null
		StripePaymentSettings.publishableKey = "pk_test_1"
		assert(!StripePaymentSettings.isConfigured)
	}

	test("isConfigured is false when either key is blank") {
		StripePaymentSettings.secretKey = "   "
		StripePaymentSettings.publishableKey = "pk_test_1"
		assert(!StripePaymentSettings.isConfigured)
	}

	test("isConfigured is true when both keys are set") {
		StripePaymentSettings.secretKey = "sk_test_1"
		StripePaymentSettings.publishableKey = "pk_test_1"
		assert(StripePaymentSettings.isConfigured)
	}

	test("publishableKeyOption returns None when the key is null or blank") {
		StripePaymentSettings.publishableKey = null
		assert(StripePaymentSettings.publishableKeyOption.isEmpty)

		StripePaymentSettings.publishableKey = "   "
		assert(StripePaymentSettings.publishableKeyOption.isEmpty)
	}

	test("publishableKeyOption returns the trimmed key when set") {
		StripePaymentSettings.publishableKey = "  pk_test_1  "
		assert(StripePaymentSettings.publishableKeyOption.contains("pk_test_1"))
	}
}
