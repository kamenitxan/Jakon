package cz.kamenitxan.jakon.shop.payments

/**
 * Fake redirect gateway used by integration tests. Discovered by [[PaymentGatewayRegistry]] classpath scanning.
 *
 * Status of a payment is derived from its external id:
 * ids starting with `completed` are completed, ids starting with `broken` make the status check fail.
 */
object TestPaymentGateway extends PaymentGateway {

	val Code = "testpay"
	val RedirectBase = "https://pay.example.com/session/"

	override def gatewayCode: String = Code

	override def isConfigured: Boolean = true

	override def initializePayment(request: PaymentRequest): PaymentInitialization = {
		val externalId = "session-" + request.shopOrder.orderNumber
		PaymentInitialization(
			provider = Code,
			flow = PaymentFlow.Redirect,
			externalPaymentId = Option(externalId),
			redirectUrl = Option(RedirectBase + externalId),
			metadata = Map("currency" -> request.currency)
		)
	}

	override def fetchPaymentStatus(externalPaymentId: String): Option[String] = {
		if (externalPaymentId.startsWith("completed")) {
			Option(PaymentStatus.Completed)
		} else if (externalPaymentId.startsWith("broken")) {
			throw new PaymentGatewayException("Test gateway failure")
		} else {
			None
		}
	}
}
