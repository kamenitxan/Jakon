package cz.kamenitxan.jakon.shop.payments

/**
 * Common contract for all payment gateway implementations.
 *
 * To add a new payment gateway, create a class (or singleton object) that extends this trait,
 * place it anywhere under the `payments` package tree and the [[PaymentGatewayRegistry]]
 * will pick it up automatically via classpath scanning.
 */
trait PaymentGateway {

	/**
	 * Short, unique, lower-case identifier of the gateway (e.g. {@code "stripe"}, {@code "manual"}).
	 * Must match the value stored in [[cz.kamenitxan.jakon.shop.entity.PaymentMethod#gatewayCode]].
	 */
	def gatewayCode: String

	/**
	 * Returns {@code true} if the gateway is fully configured and ready to process payments.
	 * Typically verifies that all required API keys are present.
	 */
	def isConfigured: Boolean

	/**
	 * Starts the payment process for the given request and returns an initialization result
	 * that the caller can use to redirect the customer or render a payment form.
	 *
	 * @param request the payment request built from the order
	 * @return a [[PaymentInitialization]] describing how to proceed with the payment
	 * @throws PaymentGatewayException if the gateway is not configured or the request is invalid
	 */
	def initializePayment(request: PaymentRequest): PaymentInitialization
}
