package cz.kamenitxan.jakon.shop.payments

/**
 * Thrown when a payment gateway operation fails due to configuration problems,
 * unsupported gateway codes, or provider-side errors.
 *
 * @param message human-readable description of the failure
 * @param cause   optional underlying exception from the gateway client or configuration layer
 */
class PaymentGatewayException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
