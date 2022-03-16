package cz.kamenitxan.jakon.utils.mail

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.JakonField
import cz.kamenitxan.jakon.core.database.annotation.{Column, OneToMany}
import cz.kamenitxan.jakon.core.database.converters.ScalaMapConverter
import cz.kamenitxan.jakon.core.model.{JakonFile, JakonObject}
import cz.kamenitxan.jakon.webui.ObjectSettings

import java.sql.{Connection, JDBCType, Statement}
import java.util.Date


class EmailEntity(u: Unit = ()) extends JakonObject {
	@Column(name = "addressTo")
	@JakonField(searched = true)
	var to: String = ""
	@JakonField
	var subject: String = ""
	@JakonField
	var sent: Boolean = false
	@JakonField(disabled = true)
	var sentDate: Date = _
	@JakonField
	var template: String = _
	@JakonField
	var lang: String = _
	@JakonField
	var emailType: String = _
	@JakonField(shownInList = false, converter = classOf[ScalaMapConverter])
	var params: Map[String, String] = _
	@OneToMany
	@JakonField(shownInList = false)
	var attachments: Seq[JakonFile] = _

	def this() = this(u = ())

	def this(template: String, to: String, subject: String, params: Map[String, String]) = {
		this(u = ())
		this.template = template
		this.to = to
		this.subject = subject
		this.lang = Settings.getDefaultLocale.getCountry
		this.params = params
	}

	def this(template: String, to: String, subject: String, params: Map[String, String], emailType: String) = {
		this(u = ())
		this.template = template
		this.to = to
		this.subject = subject
		this.params = params
		this.emailType = emailType
		this.lang = Settings.getDefaultLocale.getCountry
	}

	override val objectSettings: ObjectSettings = new ObjectSettings(icon = "fa-envelope")

	override def createObject(jid: Int, conn: Connection): Int = {
		// language=SQL
		val sql = "INSERT INTO EmailEntity (id, addressTo, subject, template, lang, emailType, params, attachments) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
		val stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
		stmt.setInt(1, jid)
		stmt.setString(2, to)
		stmt.setString(3, subject)
		stmt.setString(4, template)
		stmt.setString(5, lang)
		stmt.setString(6, emailType)
		stmt.setString(7, new ScalaMapConverter().convertToDatabaseColumn(params))
		if (attachments != null) {
			stmt.setString(8, attachments.mkString(";"))
		} else {
			stmt.setNull(8, JDBCType.VARCHAR.getVendorTypeNumber)
		}
		executeInsert(stmt)
	}

	override def updateObject(jid: Int, conn: Connection): Unit = {
		val sentDateFragment = if (sentDate != null) ", sentDate = ?" else ""
		val attachmentsFragment = if (attachments != null) ", attachments = ?" else ""
		val sql = s"UPDATE EmailEntity SET addressTo = ?, subject = ?, template = ?, lang = ?, emailType = ?, params = ?, sent = ? $sentDateFragment $attachmentsFragment WHERE id = ?"

		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, to)
		stmt.setString(2, subject)
		stmt.setString(3, template)
		stmt.setString(4, lang)
		stmt.setString(5, emailType)
		stmt.setString(6, new ScalaMapConverter().convertToDatabaseColumn(params))
		stmt.setBoolean(7, sent)
		var i = 8
		if (sentDate != null) {
			stmt.setDate(i, new java.sql.Date(sentDate.getTime))
			i += 1
		}
		if (attachments != null) {
			stmt.setString(i, attachments.mkString(";"))
			i += 1
		}
		stmt.setInt(i, jid)
		stmt.executeUpdate()
	}
}
