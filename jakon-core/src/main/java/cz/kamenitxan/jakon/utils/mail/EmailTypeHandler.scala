package cz.kamenitxan.jakon.utils.mail

import javax.mail.Message


trait EmailTypeHandler {

	def handle(emailType: String): (Message, Map[String, Any]) => Unit

	def afterSend(emailType: String): Unit
}
