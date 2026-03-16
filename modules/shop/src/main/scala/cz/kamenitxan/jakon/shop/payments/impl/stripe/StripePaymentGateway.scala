package cz.kamenitxan.jakon.shop.payments.impl.stripe

import cz.kamenitxan.jakon.shop.payments.*

/**
 * [[cz.kamenitxan.jakon.shop.payments.PaymentGateway]] implementation backed by Stripe Checkout.
 *
 * Creates a Stripe Checkout Session for the provided [[PaymentRequest]] and returns a
 * [[PaymentInitialization]] with [[PaymentFlow.Redirect]] flow containing the session URL.
 * The customer is then redirected to the Stripe-hosted payment page.
 *
 * Configuration is read from [[StripePaymentSettings]]. The gateway reports itself as
 * not configured if the secret key or publishable key are missing.
 *
 * The [[checkoutClient]] parameter allows injecting a test double to avoid real Stripe API calls.
 *
 * @param checkoutClient the Stripe Checkout client; defaults to [[DefaultStripeCheckoutClient]]
 */
class StripePaymentGateway(private val checkoutClient: StripeCheckoutClient = DefaultStripeCheckoutClient) extends PaymentGateway {

	override val gatewayCode: String = PaymentGatewayCode.Stripe

	override def isConfigured: Boolean = StripePaymentSettings.isConfigured

	override def initializePayment(request: PaymentRequest): PaymentInitialization = {
		if (!isConfigured) {
			throw new PaymentGatewayException("Stripe gateway is not configured.")
		}
		if (request.lineItems.isEmpty) {
			throw new PaymentGatewayException("Stripe payment requires at least one line item.")
		}

		val session = checkoutClient.createSession(
			StripeCheckoutSessionRequest(
				apiKey = StripePaymentSettings.secretKey,
				successUrl = request.successUrl,
				cancelUrl = request.cancelUrl,
				currency = request.currency,
				lineItems = request.lineItems,
				metadata = request.metadata,
				customerEmail = request.customerEmail
			)
		)

		PaymentInitialization(
			provider = gatewayCode,
			flow = PaymentFlow.Redirect,
			externalPaymentId = Option(session.id),
			redirectUrl = Option(session.url),
			publishableKey = StripePaymentSettings.publishableKeyOption,
			metadata = request.metadata
		)
	}
}

/** Default singleton instance using the production [[DefaultStripeCheckoutClient]]. */
object StripePaymentGateway extends StripePaymentGateway(DefaultStripeCheckoutClient)
