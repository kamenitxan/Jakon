package jakontest.core

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonUser, KeyValueEntity}
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity
import jakontest.test.HttpBrowser
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

@DoNotDiscover
class CoreMiscTest extends AnyFunSuite {

	private def unique(prefix: String) = prefix + UUID.randomUUID().toString.take(8)

	private def count(sql: String, param: String): Int = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, param)
		val rs = stmt.executeQuery()
		rs.next()
		val res = rs.getInt(1)
		stmt.close()
		res
	})

	private def createUser(): JakonUser = {
		val user = new JakonUser
		user.username = unique("mail")
		user.email = user.username + "@test.cz"
		user.password = "password"
		user.enabled = true
		user.create()
		user
	}

	private def createTemplate(): EmailTemplateEntity = {
		val tmpl = new EmailTemplateEntity
		tmpl.name = unique("TMPL")
		tmpl.from = "from@test.cz"
		tmpl.subject = "subject"
		tmpl.template = "template"
		tmpl.create()
		tmpl
	}

	test("JakonObject create rolls back and rethrows on database error") {
		val name = unique("kv")
		val first = new KeyValueEntity
		first.name = name
		first.create()

		val duplicate = new KeyValueEntity
		duplicate.name = name
		intercept[Exception](duplicate.create())
		assert(count("SELECT COUNT(*) FROM KeyValueEntity WHERE name = ?", name) == 1)
	}

	test("JakonObject update rolls back and rethrows on database error") {
		val taken = unique("kv")
		val a = new KeyValueEntity
		a.name = taken
		a.create()
		val b = new KeyValueEntity
		b.name = unique("kv")
		b.create()

		b.name = taken
		intercept[Exception](b.update())
		assert(count("SELECT COUNT(*) FROM KeyValueEntity WHERE name = ?", taken) == 1)
	}

	test("JakonUserEmailExtension sends email to filtered users") {
		val user = createUser()
		val tmpl = createTemplate()
		val resp = new HttpBrowser().get(s"/admin/object/JakonUser/sendEmail?emailType=${tmpl.name}&filter_email=${user.email}")
		assert(resp.statusCode() < 400)
		assert(count("SELECT COUNT(*) FROM EmailEntity WHERE addressTo = ?", user.email) == 1)
	}

	test("JakonUserEmailExtension sends email to single user") {
		val user = createUser()
		val tmpl = createTemplate()
		new HttpBrowser().get(s"/admin/object/JakonUser/${user.id}/sendEmail?emailType=${tmpl.name}")
		assert(count("SELECT COUNT(*) FROM EmailEntity WHERE addressTo = ?", user.email) == 1)
	}
}
