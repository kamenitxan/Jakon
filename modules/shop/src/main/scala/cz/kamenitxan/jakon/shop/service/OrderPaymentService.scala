package cz.kamenitxan.jakon.shop.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.shop.entity.{OrderPayment, ShopOrder}
import cz.kamenitxan.jakon.shop.payments.{PaymentGatewayRegistry, PaymentInitialization, PaymentStatus}

import java.sql.Connection
import java.time.LocalDateTime

/**
 * Application-level service for managing [[OrderPayment]] records.
 *
 * Provides operations to create, query and update payment records,
 * as well as to refresh the status of a payment by querying the originating gateway.
 */
object OrderPaymentService {

	/**
	 * Creates and persists a new [[OrderPayment]] record from a completed [[PaymentInitialization]].
	 *
	 * @param order  the order for which the payment was initiated
	 * @param init   the result returned by the payment gateway
	 * @param conn   implicit database connection
	 * @return the persisted [[OrderPayment]] with its generated id set
	 */
	def create(order: ShopOrder, init: PaymentInitialization)(implicit conn: Connection): OrderPayment = {
		val payment = new OrderPayment
		payment.order = order
		payment.provider = init.provider
		payment.externalPaymentId = init.externalPaymentId.getOrElse("")
		payment.status = PaymentStatus.Pending
		payment.amount = order.totalPrice
		payment.currency = init.metadata.getOrElse("currency", "")
		payment.createdAt = LocalDateTime.now()
		payment.updatedAt = LocalDateTime.now()
		payment.published = true
		payment.create()
		payment
	}

	/**
	 * Returns all payment records for the given order, ordered by creation time ascending.
	 *
	 * @param orderId the id of the [[ShopOrder]]
	 * @param conn    implicit database connection
	 * @return sequence of [[OrderPayment]] records (may be empty)
	 */
	def getByOrder(orderId: Int)(implicit conn: Connection): Seq[OrderPayment] = {
		val sql = "SELECT * FROM OrderPayment WHERE order_id = ? ORDER BY id"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, orderId)
		val result = DBHelper.select(stmt, classOf[OrderPayment]).map(_.entity)
		stmt.close()
		result
	}

	/**
	 * Returns the most recently created payment record for the given order, or [[None]] if none exists.
	 *
	 * @param orderId the id of the [[ShopOrder]]
	 * @param conn    implicit database connection
	 */
	def getLatestByOrder(orderId: Int)(implicit conn: Connection): Option[OrderPayment] = {
		val sql = "SELECT * FROM OrderPayment WHERE order_id = ? ORDER BY id DESC LIMIT 1"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, orderId)
		val result = DBHelper.select(stmt, classOf[OrderPayment]).map(_.entity).headOption
		stmt.close()
		result
	}

	/**
	 * Updates the status and updatedAt timestamp of the payment record identified by [[paymentId]].
	 *
	 * @param paymentId the id of the [[OrderPayment]] record to update
	 * @param status    new status — use [[PaymentStatus]] constants
	 * @param conn      implicit database connection
	 */
	def updateStatus(paymentId: Int, status: String)(implicit conn: Connection): Unit = {
		// language=SQL
		val sql = "UPDATE OrderPayment SET status = ?, updatedAt = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, status)
		stmt.setObject(2, LocalDateTime.now())
		stmt.setInt(3, paymentId)
		stmt.executeUpdate()
		stmt.close()
	}

	/**
	 * Asks the originating payment gateway for the current status of the payment and,
	 * if the gateway returns a result, updates the [[OrderPayment]] record accordingly.
	 *
	 * For gateways that do not support remote status checks (e.g. manual gateway),
	 * the record is left unchanged and the current status is returned as-is.
	 *
	 * @param payment the [[OrderPayment]] whose status should be refreshed
	 * @param conn    implicit database connection
	 * @return the (potentially updated) status string
	 */
	def refreshStatus(payment: OrderPayment)(implicit conn: Connection): String = {
		val gateway = PaymentGatewayRegistry.resolveByCode(payment.provider)
		gateway.flatMap(_.fetchPaymentStatus(payment.externalPaymentId)) match {
			case Some(newStatus) if newStatus != payment.status =>
				updateStatus(payment.id, newStatus)
				newStatus
			case _ =>
				payment.status
		}
	}
}
