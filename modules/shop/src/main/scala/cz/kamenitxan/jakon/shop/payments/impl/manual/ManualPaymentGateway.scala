package cz.kamenitxan.jakon.shop.payments.impl.manual

import cz.kamenitxan.jakon.shop.payments.*

/**
 * Fallback payment gateway for offline / manual payments.
 *
 * Returns a [[cz.kamenitxan.jakon.shop.payments.PaymentInitialization]] with
 * flow set to [[PaymentFlow.Manual]] and no redirect URL.
 * This gateway is always considered configured and requires no external credentials.
 */
object ManualPaymentGateway extends PaymentGateway {

	override val gatewayCode: String = PaymentGatewayCode.Manual

	override val isConfigured: Boolean = true

	override def initializePayment(request: PaymentRequest): PaymentInitialization = {
		PaymentInitialization(
			provider = gatewayCode,
			flow = PaymentFlow.Manual,
			metadata = request.metadata
		)
	}
}
