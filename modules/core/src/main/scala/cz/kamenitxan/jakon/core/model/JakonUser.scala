package cz.kamenitxan.jakon.core.model

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.ManyToOne
import cz.kamenitxan.jakon.core.database.converters.LocaleConverter
import cz.kamenitxan.jakon.utils.security.AuthUtils
import cz.kamenitxan.jakon.validation.validators.{Email, NotEmpty, Unique}
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, Statement, Types}
import java.util.Locale


/**
 * Created by TPa on 31.08.16.
 */
class JakonUser extends JakonObject with Serializable {

	@NotEmpty
	@JakonField(searched = true)
	var username: String = ""
	@NotEmpty
	@Unique
	@Email
	@JakonField(searched = true)
	var email: String = ""
	@JakonField(searched = true)
	var emailConfirmed: Boolean = false
	@JakonField(searched = true, required = false)
	var firstName: String = ""
	@JakonField(searched = true, required = false)
	var lastName: String = ""
	@NotEmpty
	@JakonField(shownInList = false, searched = true)
	var password: String = ""
	@JakonField(searched = true)
	var enabled: Boolean = false
	@NotEmpty
	@ManyToOne
	@JakonField(required = true, searched = true)
	var acl: AclRule = _
	@NotEmpty
	@JakonField(converter = classOf[LocaleConverter])
	var locale: Locale = _

	override val objectSettings: ObjectSettings = JakonUser.objectSettings

	override def createObject(jid: Int, conn: Connection): Int = {
		this.password = AuthUtils.hashPassword(this.password)
		// language=SQL
		val sql = "INSERT INTO JakonUser (id, username, email, emailConfirmed, firstName, lastName, password, enabled, acl_id, locale) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, username)
		stmt.setString(3, email)
		stmt.setBoolean(4, emailConfirmed)
		stmt.setString(5, firstName)
		stmt.setString(6, lastName)
		stmt.setString(7, password)
		stmt.setBoolean(8, enabled)
		if (acl != null) {
			stmt.setInt(9, acl.id)
		} else {
			stmt.setNull(9, Types.INTEGER)
		}
		if (locale == null) {
			locale = Settings.getDefaultLocale
		}
		stmt.setString(10, locale.toString)

		executeInsert(stmt)
	}

	override def toString: String = {
		s"JakonUser(id: $id, $email)"
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		if (!this.password.startsWith("$2a$")) {
			this.password = AuthUtils.hashPassword(this.password)
		}

		// language=SQL
		val sql = "UPDATE JakonUser SET username = ?, email = ?, emailConfirmed = ?, firstName = ?, lastName = ?, password = ?, enabled = ?, acl_id = ?, locale = ? WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, username)
		stmt.setString(2, email)
		stmt.setBoolean(3, emailConfirmed)
		stmt.setString(4, firstName)
		stmt.setString(5, lastName)
		stmt.setString(6, password)
		stmt.setBoolean(7, enabled)
		if (acl != null) {
			stmt.setInt(8, acl.id)
		} else {
			stmt.setNull(8, Types.INTEGER)
		}
		stmt.setString(9, locale.toString)
		stmt.setInt(10, jid)
		stmt.executeUpdate()
	}
}

object JakonUser {
	val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-user")
}
