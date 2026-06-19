package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.shop.payments.PaymentStatus
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.math.BigDecimal
import java.sql.{Connection, Statement, Types}
import java.time.LocalDateTime

/**
 * A payment record associated with a shop order.
 *
 * Each payment gateway invocation creates one record, allowing the application to:
 * - track the status of manual (offline) payments (admin updates the status manually),
 * - poll gateways such as Stripe for the current status of a submitted payment,
 * - keep a full history of payment attempts for a single order.
 */
class OrderPayment extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = OrderPayment.objectSettings

	/** The order this payment belongs to. */
	@ManyToOne
	@JakonField(required = true, searched = true)
	var order: ShopOrder = _

	/** Gateway code (e.g. {@code "stripe"}, {@code "manual"}). Matches [[cz.kamenitxan.jakon.shop.payments.PaymentGatewayCode]]. */
	@JakonField(searched = true)
	var provider: String = ""

	/**
	 * Gateway-side payment or session identifier (e.g. Stripe Checkout Session ID).
	 * May be empty for manual payments.
	 */
	@JakonField(searched = true)
	var externalPaymentId: String = ""

	/**
	 * Current payment status. See [[cz.kamenitxan.jakon.shop.payments.PaymentStatus]] for possible values:
	 * PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED.
	 */
	@JakonField(searched = true)
	var status: String = PaymentStatus.Pending

	/** Payment amount. */
	@JakonField(searched = true)
	var amount: BigDecimal = BigDecimal.ZERO

	/** ISO 4217 currency code (e.g. {@code "CZK"}, {@code "EUR"}). */
	@JakonField(searched = true)
	var currency: String = ""

	/** Timestamp when this record was created. */
	@JakonField(required = false)
	var createdAt: LocalDateTime = LocalDateTime.now()

	/** Timestamp of the last status update. */
	@JakonField(required = false)
	var updatedAt: LocalDateTime = LocalDateTime.now()

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql =
			"""INSERT INTO OrderPayment
			  |  (order_id, provider, externalPaymentId, status, amount, currency, createdAt, updatedAt, url, published)
			  |  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		if (order != null) {
			stmt.setInt(1, order.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		stmt.setString(2, provider)
		stmt.setString(3, externalPaymentId)
		stmt.setString(4, status)
		stmt.setBigDecimal(5, amount)
		stmt.setString(6, currency)
		stmt.setObject(7, createdAt)
		stmt.setObject(8, updatedAt)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql =
			"""UPDATE OrderPayment
			  |  SET order_id = ?, provider = ?, externalPaymentId = ?, status = ?,
			  |      amount = ?, currency = ?, createdAt = ?, updatedAt = ?, url = ?, published = ?
			  |  WHERE id = ?""".stripMargin
		val stmt = conn.prepareStatement(sql)
		if (order != null) {
			stmt.setInt(1, order.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		stmt.setString(2, provider)
		stmt.setString(3, externalPaymentId)
		stmt.setString(4, status)
		stmt.setBigDecimal(5, amount)
		stmt.setString(6, currency)
		stmt.setObject(7, createdAt)
		stmt.setObject(8, updatedAt)
		stmt.setString(9, url)
		stmt.setBoolean(10, published)
		stmt.setInt(11, jid)
		stmt.executeUpdate()
	}
}

object OrderPayment {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-credit-card", standAlone = true)
}
