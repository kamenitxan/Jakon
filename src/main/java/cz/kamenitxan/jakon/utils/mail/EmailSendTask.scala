package cz.kamenitxan.jakon.utils.mail

import java.util.Properties
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.task.AbstractTask
import javax.persistence.criteria.{CriteriaQuery, Predicate, Root}
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage}
import org.slf4j.LoggerFactory


class EmailSendTask(period: Long, unit: TimeUnit) extends AbstractTask(classOf[EmailSendTask].getSimpleName, period, unit){
	private val logger = LoggerFactory.getLogger(this.getClass)

	override def start(): Unit = {
		val session = DBHelper.getSession
		try {


			session.beginTransaction()
			val criteriaBuilder = session.getCriteriaBuilder

			val ocls: Class[EmailEntity] = classOf[EmailEntity]
			val criteriaQuery: CriteriaQuery[EmailEntity] = criteriaBuilder.createQuery(ocls)
			val from: Root[EmailEntity] = criteriaQuery.from(ocls)
			val predicate: Predicate = criteriaBuilder.equal(from.get("sent"), false)
			// TODO: waaat
			criteriaQuery.where(predicate, predicate)
			criteriaQuery.select(from)

			val emails = session.createQuery(criteriaQuery).list()
			if (emails.isEmpty) return

			val prop = new Properties()
			prop.put("mail.smtp.auth", Settings.getProperty(SettingValue.MAIL_AUTH))
			prop.put("mail.smtp.starttls.enable", Settings.getProperty(SettingValue.MAIL_TLS))
			prop.put("mail.smtp.host", Settings.getProperty(SettingValue.MAIL_HOST))
			prop.put("mail.smtp.port", Settings.getProperty(SettingValue.MAIL_PORT))
			val mailSession = Session.getInstance(prop, new Authenticator() {
				override protected def getPasswordAuthentication = new PasswordAuthentication(Settings.getProperty(SettingValue.MAIL_USERNAME), Settings.getProperty(SettingValue.MAIL_PASSWORD))
			})
			emails.forEach(e => {
				val message = new MimeMessage(mailSession)

				message.setRecipients(Message.RecipientType.TO, "to@gmail.com")
				message.setSubject(e.subject)

				val msg = "This is my first email using JavaMailer"
				if (e.template != null) {
					val tmpl = session.get(classOf[EmailTemplateEntity], e.template)

					message.setFrom(new InternetAddress(tmpl.from))

					val te = Settings.getTemplateEngine
					te.renderTemplate(tmpl.template, e.params)
					val mimeBodyPart = new MimeBodyPart()
					mimeBodyPart.setContent(msg, "text/html")
				}

				if (e.emailType != null) {
					val fun = Settings.getEmailTypeHandler.handle(e.emailType)
					fun.apply(message, e.params)
				}
				Transport.send(message)
			})

		} catch {
			case ex : Exception => logger.error("Error while sending email", ex)
		} finally {
			session.getTransaction.commit()
			session.close()
		}
	}
}