package utils.mail

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import org.scalatest.FunSuite

class EmailTest extends FunSuite {

	test("registrationEmailTest") {
		val user = new JakonUser()
		user.email = "test@test.com"
		Authentication.sendRegistrationEmail(user)
	}
}
