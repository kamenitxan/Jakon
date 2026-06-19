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
 * Záznam o platbě přiřazené k objednávce.
 *
 * Každé volání platební brány vytvoří jeden záznam. Entita umožňuje:
 * - evidovat stav manuálních plateb (admin mění status ručně),
 * - dotazovat se brány (např. Stripe) na aktuální stav odeslané platby,
 * - sledovat historii pokusů o platbu pro jednu objednávku.
 */
class OrderPayment extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = OrderPayment.objectSettings

	/** Objednávka, ke které tato platba patří. */
	@ManyToOne
	@JakonField(required = true, searched = true)
	var order: ShopOrder = _

	/** Kód platební brány (např. "stripe", "manual"). Odpovídá [[cz.kamenitxan.jakon.shop.payments.PaymentGatewayCode]]. */
	@JakonField(searched = true)
	var provider: String = ""

	/**
	 * Identifikátor platby na straně brány (např. Stripe Checkout Session ID).
	 * U manuálních plateb může být prázdný.
	 */
	@JakonField(searched = true)
	var externalPaymentId: String = ""

	/**
	 * Aktuální stav platby. Možné hodnoty viz [[cz.kamenitxan.jakon.shop.payments.PaymentStatus]]:
	 * PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED.
	 */
	@JakonField(searched = true)
	var status: String = PaymentStatus.Pending

	/** Částka platby. */
	@JakonField(searched = true)
	var amount: BigDecimal = BigDecimal.ZERO

	/** Měna platby (ISO 4217, např. "CZK", "EUR"). */
	@JakonField(searched = true)
	var currency: String = ""

	/** Datum a čas vytvoření záznamu. */
	@JakonField(required = false)
	var createdAt: LocalDateTime = LocalDateTime.now()

	/** Datum a čas poslední aktualizace stavu. */
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
