package cz.kamenitxan.jakon.shop.payments

import cz.kamenitxan.jakon.shop.entity.Order

import java.math.BigDecimal

/**
 * A single line item within a payment request, representing one product, fee or charge.
 *
 * @param name        display name shown to the customer on the payment page
 * @param quantity    number of units; defaults to 1
 * @param unitPrice   price per single unit before multiplication by quantity
 * @param description optional additional description shown to the customer
 */
case class PaymentLineItem(
	name: String,
	quantity: Long = 1L,
	unitPrice: BigDecimal,
	description: Option[String] = Option.empty
)

/**
 * Encapsulates all data required to initiate a payment through any gateway.
 *
 * @param order         the order being paid
 * @param lineItems     individual items, fees and charges that make up the total
 * @param successUrl    URL the customer is redirected to after a successful payment
 * @param cancelUrl     URL the customer is redirected to when the payment is cancelled
 * @param currency      ISO 4217 currency code (e.g. {@code "czk"}, {@code "eur"})
 * @param metadata      arbitrary key-value pairs forwarded to the gateway for reconciliation
 * @param customerEmail optional customer e-mail address passed to the gateway
 */
case class PaymentRequest(
	order: Order,
	lineItems: Seq[PaymentLineItem],
	successUrl: String,
	cancelUrl: String,
	currency: String,
	metadata: Map[String, String],
	customerEmail: Option[String]
)

/**
 * Result returned by a gateway after successfully initialising a payment session.
 *
 * @param provider          gateway code that produced this result (e.g. {@code "stripe"})
 * @param flow              describes how the payment should proceed; see [[PaymentFlow]]
 * @param externalPaymentId provider-side session or payment identifier for reconciliation
 * @param redirectUrl       URL to redirect the customer to (used when {@code flow == "redirect"})
 * @param publishableKey    gateway publishable/client-side key required by front-end SDKs
 * @param metadata          metadata echoed back from the original request
 */
case class PaymentInitialization(
	provider: String,
	flow: String,
	externalPaymentId: Option[String] = Option.empty,
	redirectUrl: Option[String] = Option.empty,
	publishableKey: Option[String] = Option.empty,
	metadata: Map[String, String] = Map.empty
)

/** Well-known gateway code constants used to identify payment providers. */
object PaymentGatewayCode {
	/** Manual / offline payment — no online gateway involved. */
	final val Manual = "manual"
	/** Stripe payment gateway. */
	final val Stripe = "stripe"
}

/** Payment flow constants that describe how the customer completes the payment. */
object PaymentFlow {
	/** The customer pays offline; no online redirect is needed. */
	final val Manual = "manual"
	/** The customer is redirected to an external payment page. */
	final val Redirect = "redirect"
}
