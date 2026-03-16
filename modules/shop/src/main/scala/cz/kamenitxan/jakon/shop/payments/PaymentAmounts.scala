package cz.kamenitxan.jakon.shop.payments

import java.math.{BigDecimal, RoundingMode}
import java.util.Currency

/**
 * Utility methods for converting monetary amounts to the minor-unit representation
 * required by payment gateways (e.g. Stripe expects amounts in cents / haléře).
 */
object PaymentAmounts {

	/**
	 * Converts a decimal amount to the minor unit (smallest currency subdivision) as a long value.
	 *
	 * For example, {@code toMinorUnits(new BigDecimal("12.50"), "EUR")} returns {@code 1250}.
	 * Zero-decimal currencies (e.g. JPY) are returned unchanged.
	 *
	 * @param amount       the amount to convert; must not be {@code null}
	 * @param currencyCode ISO 4217 currency code (case-insensitive)
	 * @return the amount in minor units
	 * @throws ArithmeticException if the scaled value cannot be represented as a long
	 */
	def toMinorUnits(amount: BigDecimal, currencyCode: String): Long = {
		val scale = Currency.getInstance(currencyCode.toUpperCase).getDefaultFractionDigits
		amount
			.movePointRight(scale)
			.setScale(0, RoundingMode.HALF_UP)
			.longValueExact()
	}
}
