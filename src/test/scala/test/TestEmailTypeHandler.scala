package test

import cz.kamenitxan.jakon.utils.mail.EmailTypeHandler
import javax.mail.Message

class TestEmailTypeHandler extends EmailTypeHandler {

	override def handle(emailType: String): (Message, Map[String, Any]) => Unit = (_, _) => {

	}

	override def afterSend(emailType: String): Unit = {

	}
}
