package cz.kamenitxan.jakon.payment

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}

@Configuration
object PaymentSettings {

	@ConfigurationValue(name = "PAYMENT.enabled", required = true, defaultValue = "false")
	private var paymentsEnabled: Boolean = _
	/**
	  * test: https://gw.sandbox.gopay.com/api
	  * prod: https://gate.gopay.cz/api
	  */
	@ConfigurationValue(name = "PAYMENT.api_url", required = false)
	private var apiUrl: String = _

	def isPaymentsEnabled = paymentsEnabled

	def getApiUrl: String = apiUrl

}
