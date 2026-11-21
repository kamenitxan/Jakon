package cz.kamenitxan.jakon.shop.payments

import org.scalatest.funsuite.AnyFunSuite

import java.math.BigDecimal

class PaymentAmountsTest extends AnyFunSuite {

	test("converts a two-decimal currency amount to minor units") {
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("12.50"), "EUR") == 1250L)
	}

	test("is case-insensitive with respect to the currency code") {
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("12.50"), "czk") == 1250L)
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("12.50"), "CZK") == 1250L)
	}

	test("leaves zero-decimal currencies unchanged") {
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("1500"), "JPY") == 1500L)
	}

	test("rounds half up when the amount has more decimals than the currency allows") {
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("12.505"), "EUR") == 1251L)
		assert(PaymentAmounts.toMinorUnits(new BigDecimal("12.504"), "EUR") == 1250L)
	}

	test("handles zero amounts") {
		assert(PaymentAmounts.toMinorUnits(BigDecimal.ZERO, "EUR") == 0L)
	}

	test("throws ArithmeticException when the scaled value overflows a long") {
		val huge = new BigDecimal("99999999999999999999.99")
		assertThrows[ArithmeticException] {
			PaymentAmounts.toMinorUnits(huge, "EUR")
		}
	}
}
