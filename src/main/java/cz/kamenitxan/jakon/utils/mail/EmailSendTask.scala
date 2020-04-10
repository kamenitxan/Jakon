package cz.kamenitxan.jakon.utils.mail

import java.util.concurrent.TimeUnit
import java.util.{Date, Properties}

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.task.AbstractTask
import cz.kamenitxan.jakon.logging.Logger
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}

object EmailSendTask {
	val TMPL_LANG = "tmplLanguage"

	private val UNSENT_SQL = "SELECT * FROM EmailEntity WHERE sent = 0"
	private val SELECT_EMAIL_TMPL_SQL = "SELECT addressFrom, template FROM EmailTemplateEntity WHERE name = ? OR name = ? LIMIT 1"
}

class EmailSendTask(period: Long, unit: TimeUnit) extends AbstractTask(period, unit) {

	override def start(): Unit = {
		val conn = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()

			val emails = DBHelper.select(stmt, EmailSendTask.UNSENT_SQL, classOf[EmailEntity]).map(qr => qr.entity)
			if (emails.isEmpty) return

			val prop = new Properties()
			prop.put("mail.smtp.auth", Settings.getEmailAuth)
			if (Settings.getEmailTls) {
				prop.put("mail.smtp.starttls.enable", "true")
			}
			prop.put("mail.smtp.host", Settings.getEmailHost)
			prop.put("mail.smtp.port", Settings.getEmailPort)
			val mailSession = Session.getInstance(prop, new Authenticator() {
				override protected def getPasswordAuthentication = new PasswordAuthentication(Settings.getEmailUserName, Settings.getEmailPassword)
			})
			emails.foreach(e => {
				try {
					val message = new MimeMessage(mailSession)
					message.setRecipients(Message.RecipientType.TO, e.to)
					message.setSubject(e.subject)
					val forceBcc = Settings.getEmailForceBcc
					if (forceBcc != null) {
						message.setRecipients(Message.RecipientType.BCC, forceBcc)
					}


					if (e.template != null) {
						val tmplLangSuffix = if (e.params != null) {
							e.params.getOrElse("tmplLanguage", "")
						} else {
							""
						}
						val stmt = conn.prepareStatement(EmailSendTask.SELECT_EMAIL_TMPL_SQL)
						stmt.setString(1, e.template + "_" + tmplLangSuffix)
						stmt.setString(2, e.template)
						val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity

						if (Settings.getDeployMode.equals(DeployMode.DEVEL)) {
							message.setFrom(new InternetAddress(Settings.getEmailUserName))
						} else {
							message.setFrom(new InternetAddress(tmpl.from))
						}


						val te = Settings.getTemplateEngine
						val renderedMessage = te.renderTemplate(tmpl.template, e.params)
						message.setContent(renderedMessage, "text/html; charset=UTF-8")
					}

					if (e.emailType != null) {
						val fun = Settings.getEmailTypeHandler.handle(e.emailType)
						fun.apply(message, e.params)
					}
					Transport.send(message)
					e.sent = true
					e.sentDate = new Date()
					e.update()
					Settings.getEmailTypeHandler.afterSend(e.emailType)
				} catch {
					case ex: Exception =>
						Logger.error(s"Failed to send email (id:${e.id})", ex)
						throw ex
				}
			})
		} finally {
			conn.close()
		}
	}
}
