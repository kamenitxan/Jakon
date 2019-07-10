package utils.mail

import java.util.Date

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import org.scalatest.FunSuite

class EmailTest extends FunSuite {

	test("registrationEmailTest") {
		val user = new JakonUser()
		user.email = "test@test.com"
		user.create()
		Authentication.sendRegistrationEmail(user)
	}

	test("EmailEntity") {
		val params = Map[String, String](
			"param" -> "test"
		)
		val e = new EmailEntity("tmpl", "to", "subject", params, "TYPE")
		e.create()

		e.sent = true
		e.sentDate = new Date()
		e.update()
	}

	test("EmailTemplateEntity") {
		val e = new EmailTemplateEntity()
		e.name = "name"
		e.from = "from"
		e.subject = "subj"
		e.template = "templ"
		e.create()

		e.update()
	}

}
