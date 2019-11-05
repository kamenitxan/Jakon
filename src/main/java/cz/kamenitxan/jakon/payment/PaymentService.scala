package cz.kamenitxan.jakon.payment

import cz.gopay.api.v3.GPClientException
import cz.gopay.api.v3.impl.apacheclient.HttpClientGPConnector
import cz.gopay.api.v3.model.common.Currency
import cz.gopay.api.v3.model.payment.{Lang, PaymentFactory}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.payment.entity.PaymentTransaction
import org.slf4j.LoggerFactory

object PaymentService {
	private val connector = HttpClientGPConnector.build(PaymentSettings.getApiUrl)
	private val logger = LoggerFactory.getLogger(this.getClass)

	def createPayment(transaction: PaymentTransaction) = {
		val payment = PaymentFactory.createBasePaymentBuilder()
		  .order(transaction.id.toString, transaction.amount, Currency.EUR, "DESCRIPTION")
		  /*.addItem(
			  "ITEM_NAME",
			  "AMOUNT",
			  "FEE",
			  "COUNT")*/
		  .addAdditionalParameter(
			  "Key",
			  "VALUE")
		  .withCallback(
			  "RETURN_URL",
			  "NOTIFY_URL")
		  /*.payer(
			  "Payer")*/
		  .inLang(Lang.EN)
		  .toEshop(
			  0L)
		  .build();
		try {
			val result = connector.createPayment(payment);
			result.getTarget
		} catch {
			case ex: GPClientException => logger.error("Failed to create payment", ex)
		}
	}

	def getTransactionById(id: Int): PaymentTransaction = {
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM PaymentTransaction WHERE id = ?")
			stmt.setInt(1, id)
			DBHelper.selectSingleDeep(stmt, classOf[PaymentTransaction])
		})
	}
}
