package cz.kamenitxan.jakon.shop.payments.impl.stripe

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}

/**
 * Configuration holder for Stripe payment gateway settings.
 *
 * Values are loaded automatically from the application properties file via the
 * {@code @Configuration} / {@code @ConfigurationValue} scanning mechanism.
 *
 * Required properties (set in {@code jakon_config.properties}):
 * <pre>
 *   PAYMENT.stripe.secretKey      = sk_live_...
 *   PAYMENT.stripe.publishableKey = pk_live_...
 * </pre>
 *
 * Optional properties:
 * <pre>
 *   PAYMENT.stripe.webhookSecret  = whsec_...
 *   PAYMENT.currency              = czk      (default: czk)
 * </pre>
 */
@Configuration
object StripePaymentSettings {

	/** Stripe secret API key used for server-side requests. */
	@ConfigurationValue(name = "PAYMENT.stripe.secretKey", required = false)
	var secretKey: String = _

	/** Stripe publishable key exposed to the front-end for client-side SDKs. */
	@ConfigurationValue(name = "PAYMENT.stripe.publishableKey", required = false)
	var publishableKey: String = _

	/** Stripe webhook signing secret used to verify incoming webhook events. */
	@ConfigurationValue(name = "PAYMENT.stripe.webhookSecret", required = false)
	var webhookSecret: String = _

	/** ISO 4217 currency code used when no explicit currency is supplied. Defaults to {@code czk}. */
	@ConfigurationValue(name = "PAYMENT.currency", required = false, defaultValue = "czk")
	var currency: String = "czk"

	/**
	 * Returns {@code true} if both the secret key and the publishable key are non-blank,
	 * meaning the gateway is ready to process payments.
	 */
	def isConfigured: Boolean = {
		Option(secretKey).exists(_.trim.nonEmpty) && Option(publishableKey).exists(_.trim.nonEmpty)
	}

	/**
	 * Returns the publishable key wrapped in {@code Some}, or {@code None} if it is not set.
	 * Useful for passing the key to front-end templates without null checks.
	 */
	def publishableKeyOption: Option[String] = {
		Option(publishableKey).map(_.trim).filter(_.nonEmpty)
	}
}
