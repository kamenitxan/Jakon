package cz.kamenitxan.jakon.utils.mail

import java.util.Properties
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.configuration.{DeployMode, SettingValue, Settings}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.task.AbstractTask
import javax.persistence.criteria.{CriteriaQuery, Expression, Predicate, Root}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage}
import org.hibernate.criterion.Restrictions
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object EmailSendTask {
	val TMPL_LANG = "tmplLanguage"

	val UNSENT_SQL = "SELECT * FROM EmailEntity WHERE sent = 0"
}

class EmailSendTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[EmailSendTask].getSimpleName, period, unit){
	private val logger = LoggerFactory.getLogger(this.getClass)

	override def start(): Unit = {
		val conn = DBHelper.getConnection
		try {
			val stmt = conn.prepareStatement(EmailSendTask.UNSENT_SQL)
			DBHelper.select(stmt, classOf[EmailEntity])

			val emails = DBHelper.select(stmt, classOf[EmailEntity]).map(qr => qr.entity.asInstanceOf[EmailEntity])
			if (emails.isEmpty) return

			val prop = new Properties()
			prop.put("mail.smtp.auth", Settings.getProperty(SettingValue.MAIL_AUTH))
			prop.put("mail.smtp.starttls.enable", Settings.getProperty(SettingValue.MAIL_TLS))
			prop.put("mail.smtp.host", Settings.getProperty(SettingValue.MAIL_HOST))
			prop.put("mail.smtp.port", Settings.getProperty(SettingValue.MAIL_PORT))
			val mailSession = Session.getInstance(prop, new Authenticator() {
				override protected def getPasswordAuthentication = new PasswordAuthentication(Settings.getProperty(SettingValue.MAIL_USERNAME), Settings.getProperty(SettingValue.MAIL_PASSWORD))
			})
			//TODO: remove filter
			emails.filter(e => !e.sent).foreach(e => {
				val message = new MimeMessage(mailSession)
				message.setRecipients(Message.RecipientType.TO, e.to)
				message.setSubject(e.subject)


				if (e.template != null) {
					val criteria = getSession.createCriteria(classOf[EmailTemplateEntity])
					val tmpl: EmailTemplateEntity = criteria.add(Restrictions.eq("name", e.template) ).uniqueResult().asInstanceOf[EmailTemplateEntity]

					if (Settings.getDeployMode.equals(DeployMode.DEVEL)) {
						message.setFrom(new InternetAddress(Settings.getProperty(SettingValue.MAIL_USERNAME)))
					} else {
						message.setFrom(new InternetAddress(tmpl.from))
					}

					val tmplLangSuffix = e.params.getOrElse("tmplLanguage", "")
					val te = Settings.getTemplateEngine
					te.renderTemplate(tmpl.template + tmplLangSuffix, e.params)
					message.setContent(tmpl.template, "text/html")
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
