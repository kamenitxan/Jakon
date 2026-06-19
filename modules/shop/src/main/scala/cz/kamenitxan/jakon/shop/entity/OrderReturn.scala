package cz.kamenitxan.jakon.shop.entity

import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}
import java.time.LocalDateTime

/**
 * A customer's withdrawal from contract (return request) linked to a shop order.
 *
 * Possible [[status]] values are defined in [[OrderReturnStatus]].
 */
class OrderReturn extends JakonObject with Serializable {

	override val objectSettings: ObjectSettings = OrderReturn.objectSettings

	/** The order the customer is returning items from. */
	@ManyToOne
	@JakonField(required = true, searched = true)
	var order: ShopOrder = _

	/** Unique return identifier generated at creation (e.g. {@code RET-2026-123456}). */
	@JakonField(searched = true)
	var returnNumber: String = ""

	/**
	 * Current processing status.
	 * See [[OrderReturnStatus]] for allowed values: PENDING, APPROVED, REJECTED, COMPLETED.
	 */
	@JakonField(searched = true)
	var status: String = OrderReturnStatus.Pending

	/** Customer-provided reason for the return. See [[OrderReturnReason]] for allowed values. */
	@JakonField(required = false)
	var reason: String = ""

	/** Customer's bank account number for the refund transfer. */
	@JakonField(required = false, searched = true)
	var bankAccount: String = ""

	/** Internal admin note, not visible to the customer. */
	@JakonField(required = false)
	var adminNote: String = ""

	/** Timestamp when this return request was created. */
	@JakonField(required = false)
	var createdAt: LocalDateTime = LocalDateTime.now()

	override def createObject(conn: Connection): Int = {
		// language=SQL
		val sql =
			"""INSERT INTO OrderReturn
			  |  (order_id, returnNumber, status, reason, bankAccount, adminNote, createdAt, url, published)
			  |  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		if (order != null) {
			stmt.setInt(1, order.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		stmt.setString(2, returnNumber)
		stmt.setString(3, status)
		stmt.setString(4, reason)
		stmt.setString(5, bankAccount)
		stmt.setString(6, adminNote)
		stmt.setObject(7, createdAt)
		stmt.setString(8, url)
		stmt.setBoolean(9, published)
		val generatedId = executeInsert(stmt)
		this.id = generatedId
		generatedId
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		// language=SQL
		val sql =
			"""UPDATE OrderReturn
			  |  SET order_id = ?, returnNumber = ?, status = ?, reason = ?, bankAccount = ?,
			  |      adminNote = ?, createdAt = ?, url = ?, published = ?
			  |  WHERE id = ?""".stripMargin
		val stmt = conn.prepareStatement(sql)
		if (order != null) {
			stmt.setInt(1, order.id)
		} else {
			stmt.setNull(1, Types.INTEGER)
		}
		stmt.setString(2, returnNumber)
		stmt.setString(3, status)
		stmt.setString(4, reason)
		stmt.setString(5, bankAccount)
		stmt.setString(6, adminNote)
		stmt.setObject(7, createdAt)
		stmt.setString(8, url)
		stmt.setBoolean(9, published)
		stmt.setInt(10, jid)
		stmt.executeUpdate()
	}
}

object OrderReturn {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-undo", standAlone = true)
}

/** Allowed values for [[OrderReturn#status]]. */
object OrderReturnStatus {
	/** Return request submitted, awaiting admin review. */
	final val Pending = "PENDING"
	/** Return approved by admin, awaiting shipment. */
	final val Approved = "APPROVED"
	/** Return rejected by admin. */
	final val Rejected = "REJECTED"
	/** Return fully processed and refund issued. */
	final val Completed = "COMPLETED"
}

/** Allowed values for [[OrderReturn#reason]]. */
object OrderReturnReason {
	final val Defective      = "DEFECTIVE"
	final val WrongItem      = "WRONG_ITEM"
	final val NotAsDescribed = "NOT_AS_DESCRIBED"
	final val ChangedMind    = "CHANGED_MIND"
	final val Other          = "OTHER"

	val all: Seq[String] = Seq(Defective, WrongItem, NotAsDescribed, ChangedMind, Other)
}
