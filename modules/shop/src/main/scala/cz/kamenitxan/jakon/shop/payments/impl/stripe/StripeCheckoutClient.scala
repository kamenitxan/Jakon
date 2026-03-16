package cz.kamenitxan.jakon.shop.payments.impl.stripe

import com.stripe.exception.StripeException
import com.stripe.model.checkout.Session
import com.stripe.net.RequestOptions
import com.stripe.param.checkout.SessionCreateParams
import cz.kamenitxan.jakon.shop.payments.{PaymentAmounts, PaymentGatewayException, PaymentLineItem}

import scala.util.control.NonFatal

/**
 * Data required to create a Stripe Checkout Session.
 *
 * @param apiKey        Stripe secret API key used for this request
 * @param successUrl    URL Stripe redirects the customer to after a successful payment
 * @param cancelUrl     URL Stripe redirects the customer to when the payment is cancelled
 * @param currency      ISO 4217 currency code in lower case (e.g. {@code "czk"})
 * @param lineItems     items, fees and charges to display on the Stripe Checkout page
 * @param metadata      key-value metadata attached to the Stripe session for reconciliation
 * @param customerEmail optional customer e-mail pre-filled on the Stripe Checkout page
 */
case class StripeCheckoutSessionRequest(
	apiKey: String,
	successUrl: String,
	cancelUrl: String,
	currency: String,
	lineItems: Seq[PaymentLineItem],
	metadata: Map[String, String],
	customerEmail: Option[String]
)

/**
 * Result returned after successfully creating a Stripe Checkout Session.
 *
 * @param id  Stripe session identifier (e.g. {@code cs_test_...})
 * @param url URL to redirect the customer to in order to complete the payment
 */
case class StripeCheckoutSessionResponse(
	id: String,
	url: String
)

/**
 * Thin abstraction over the Stripe Checkout Session API.
 * Swap the implementation in tests to avoid real network calls.
 */
trait StripeCheckoutClient {

	/**
	 * Creates a Stripe Checkout Session and returns its identifier and redirect URL.
	 *
	 * @param request parameters for the session
	 * @return the created session's id and URL
	 * @throws PaymentGatewayException if the Stripe API returns an error
	 */
	def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse
}

/**
 * Default production implementation of [[StripeCheckoutClient]] that calls the Stripe Java SDK.
 */
object DefaultStripeCheckoutClient extends StripeCheckoutClient {

	override def createSession(request: StripeCheckoutSessionRequest): StripeCheckoutSessionResponse = {
		try {
			val sessionBuilder = SessionCreateParams.builder()
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(request.successUrl)
				.setCancelUrl(request.cancelUrl)

			request.customerEmail.foreach(sessionBuilder.setCustomerEmail)
			request.metadata.foreach(entry => sessionBuilder.putMetadata(entry._1, entry._2))
			request.lineItems.foreach(item => sessionBuilder.addLineItem(createLineItem(item, request.currency)))

			val session = Session.create(
				sessionBuilder.build(),
				RequestOptions.builder().setApiKey(request.apiKey).build()
			)

			StripeCheckoutSessionResponse(session.getId, session.getUrl)
		} catch {
			case ex: StripeException => throw new PaymentGatewayException("Stripe checkout session creation failed.", ex)
			case NonFatal(ex) => throw new PaymentGatewayException("Unexpected error while creating Stripe checkout session.", ex)
		}
	}

	/**
	 * Converts a [[PaymentLineItem]] to the Stripe SDK line-item parameter object.
	 *
	 * @param item     the line item to convert
	 * @param currency ISO 4217 currency code in lower case
	 * @return Stripe [[SessionCreateParams.LineItem]]
	 */
	private def createLineItem(item: PaymentLineItem, currency: String): SessionCreateParams.LineItem = {
		val productDataBuilder = SessionCreateParams.LineItem.PriceData.ProductData.builder()
			.setName(item.name)
		item.description.foreach(productDataBuilder.setDescription)

		SessionCreateParams.LineItem.builder()
			.setQuantity(item.quantity)
			.setPriceData(
				SessionCreateParams.LineItem.PriceData.builder()
					.setCurrency(currency)
					.setUnitAmount(PaymentAmounts.toMinorUnits(item.unitPrice, currency))
					.setProductData(productDataBuilder.build())
					.build()
			)
			.build()
	}
}
