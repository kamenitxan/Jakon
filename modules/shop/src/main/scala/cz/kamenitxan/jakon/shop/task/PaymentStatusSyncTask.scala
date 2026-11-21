package cz.kamenitxan.jakon.shop.task

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.task.AbstractTask
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.shop.entity.OrderPayment
import cz.kamenitxan.jakon.shop.payments.PaymentStatus
import cz.kamenitxan.jakon.shop.service.OrderPaymentService

import java.sql.Connection
import java.util.concurrent.TimeUnit

/**
 * Periodically polls payment gateways for the current status of PENDING payments.
 *
 * For each [[OrderPayment]] in state [[PaymentStatus.Pending]] with a non-empty
 * [[OrderPayment#externalPaymentId]], calls [[OrderPaymentService.refreshStatus]].
 * When the status transitions to [[PaymentStatus.Completed]], also sets
 * [[cz.kamenitxan.jakon.shop.entity.ShopOrder#isPaid]] to {@code true}.
 *
 * Gateways that do not support status polling (e.g. ManualPaymentGateway) are skipped —
 * their [[cz.kamenitxan.jakon.shop.payments.PaymentGateway#fetchPaymentStatus]] returns [[None]].
 *
 * @param period how often the task should run
 * @param unit   time unit for {@code period}
 */
class PaymentStatusSyncTask(period: Long, unit: TimeUnit) extends AbstractTask(period, unit) {

	override def start(): Unit = {
		DBHelper.withDbConnection { implicit conn =>
			val pendingPayments = loadPendingPayments()
			if (pendingPayments.nonEmpty) {
				Logger.info(s"PaymentStatusSyncTask: checking ${pendingPayments.size} pending payment(s)")
			}
			pendingPayments.foreach { payment =>
				try {
					val newStatus = OrderPaymentService.refreshStatus(payment)
					if (newStatus == PaymentStatus.Completed && newStatus != payment.status) {
						Logger.info(s"PaymentStatusSyncTask: payment ${payment.id} completed (externalId=${payment.externalPaymentId})")
					}
				} catch {
					case ex: Exception =>
						Logger.error(s"PaymentStatusSyncTask: failed to refresh status for payment ${payment.id} (externalId=${payment.externalPaymentId})", ex)
				}
			}
		}
	}

	/** Loads all PENDING payments with a non-empty {@code externalPaymentId}. */
	private def loadPendingPayments()(implicit conn: Connection): Seq[OrderPayment] = {
		// language=SQL
		val sql =
			"""SELECT * FROM OrderPayment
			  | WHERE status = ? AND externalPaymentId IS NOT NULL AND externalPaymentId != ''""".stripMargin
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, PaymentStatus.Pending)
		val all = DBHelper.select(stmt, classOf[OrderPayment]).map(_.entity)
		stmt.close()
		all
	}
}
