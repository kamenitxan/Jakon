package jakontest.utils.mail

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.AclRuleService
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.controller.pagelets.JakonRegistrationPagelet
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite
import jakontest.test.TestEmailTypeHandler

import java.util.Date
import java.util.concurrent.TimeUnit
import scala.util.Random

@DoNotDiscover
class EmailTest extends AnyFunSuite {

	test("registrationEmailTest") {
		DBHelper.withDbConnection(implicit conn => {
			val user = new JakonUser()
			user.firstName = "registrationEmailTest"
			user.lastName = "lastName"
			user.email = "test@test.com" + Random.nextInt()
			user.acl = AclRuleService.getByName("Admin").get
			user.create()
			new JakonRegistrationPagelet().sendRegistrationEmail(user)
		})
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

	test("send email") {
		val params = Map[String, String](
			"param" -> "test"
		)
		val e = new EmailEntity("tmpl", "to", "subject", params, "TYPE")
		e.attachments = Seq.empty
		e.create()

		// smtp does not work in travis
		assertThrows[Throwable](new EmailSendTask(1, TimeUnit.DAYS).start())

		Settings.setEmailTypeHandler(new TestEmailTypeHandler)
		Settings.setDeployMode(DeployMode.PRODUCTION)
		val e2 = new EmailEntity("tmpl", "to", "subject", null, "TYPE2")
		e2.create()
		assertThrows[Throwable](new EmailSendTask(1, TimeUnit.DAYS).start())

		Settings.setDeployMode(DeployMode.DEVEL)
	}

}
