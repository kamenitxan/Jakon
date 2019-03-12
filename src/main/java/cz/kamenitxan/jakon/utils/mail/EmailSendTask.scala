package cz.kamenitxan.jakon.utils.mail

import java.util.Properties
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.task.AbstractTask
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}
import org.slf4j.LoggerFactory

object EmailSendTask {
	val TMPL_LANG = "tmplLanguage"

	private val UNSENT_SQL = "SELECT * FROM EmailEntity WHERE sent = 0"
	private val SELECT_EMAIL_TMPL_SQL = "SELECT addressFrom, template FROM EmailTemplateEntity WHERE name = ?"
}

class EmailSendTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[EmailSendTask].getSimpleName, period, unit){
	private val logger = LoggerFactory.getLogger(this.getClass)

	override def start(): Unit = {
		val conn = DBHelper.getConnection
		try {
			val stmt = conn.createStatement()

			val emails = DBHelper.select(stmt, EmailSendTask.UNSENT_SQL, classOf[EmailEntity]).map(qr => qr.entity.asInstanceOf[EmailEntity])
			if (emails.isEmpty) return

			val prop = new Properties()
			prop.put("mail.smtp.auth", Settings.getEmailAuth)
			//prop.put("mail.smtp.starttls.enable", Settings.getProperty(SettingValue.MAIL_TLS))
			prop.put("mail.smtp.host", Settings.getEmailHost)
			prop.put("mail.smtp.port", Settings.getEmailPort)
			val mailSession = Session.getInstance(prop, new Authenticator() {
				override protected def getPasswordAuthentication = new PasswordAuthentication(Settings.getEmailUserName, Settings.getEmailPassword)
			})
			emails.foreach(e => {
				val message = new MimeMessage(mailSession)
				message.setRecipients(Message.RecipientType.TO, e.to)
				message.setSubject(e.subject)
				val force_bcc = Settings.getEmailForceBcc
				if (force_bcc != null) {
					message.setRecipients(Message.RecipientType.BCC, force_bcc)
				}


				if (e.template != null) {
					val stmt = conn.prepareStatement(EmailSendTask.SELECT_EMAIL_TMPL_SQL)
					stmt.setString(1, e.template)
					val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity.asInstanceOf[EmailTemplateEntity]

					if (Settings.getDeployMode.equals(DeployMode.DEVEL)) {
						message.setFrom(new InternetAddress(Settings.getEmailUserName))
					} else {
						message.setFrom(new InternetAddress(tmpl.from))
					}

					val tmplLangSuffix = e.params.getOrElse("tmplLanguage", "")
					val te = Settings.getTemplateEngine
					val renderedMessage = te.renderTemplate(tmpl.template + tmplLangSuffix, e.params)
					message.setContent(renderedMessage, "text/html")
				}

				if (e.emailType != null) {
					val fun = Settings.getEmailTypeHandler.handle(e.emailType)
					fun.apply(message, e.params)
				}
				Transport.send(message)
				e.sent = true
				e.update()
			})
		} finally {
			conn.close()
		}
	}
}
