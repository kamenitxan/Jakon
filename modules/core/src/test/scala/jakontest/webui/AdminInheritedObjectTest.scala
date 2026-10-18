package jakontest.webui

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.AclRule
import jakontest.test.HttpBrowser
import jakontest.test.HttpBrowser.location
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

/**
 * Administration of objects inheriting from another object (TestExtUser and TestExtNoFields extend JakonUser).
 * Data of such object are stored in parent and child table.
 */
@DoNotDiscover
class AdminInheritedObjectTest extends AnyFunSuite {

	private lazy val acl: AclRule = {
		val acl = new AclRule
		acl.name = "inherited" + UUID.randomUUID().toString.take(8)
		acl.create()
		acl
	}

	private def unique(prefix: String) = prefix + UUID.randomUUID().toString.take(8)

	private def queryString(sql: String, param: Any): String = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, String.valueOf(param))
		val rs = stmt.executeQuery()
		val res = if (rs.next()) rs.getString(1) else null
		stmt.close()
		res
	})

	private def count(sql: String, param: Any): Int = Option(queryString(sql, param)).map(_.toInt).getOrElse(0)

	/** Form of JakonUser fields with given username, email is derived from it. */
	private def userForm(username: String, extra: (String, String)*): Seq[(String, String)] = Seq(
		"username" -> username,
		"email" -> s"$username@test.cz",
		"firstName" -> "Inherited",
		"lastName" -> "User",
		"password" -> "password123",
		"enabled" -> "true",
		"acl" -> acl.id.toString,
		"locale" -> "cs_CZ"
	) ++ extra

	private def createThroughAdmin(objectName: String, username: String, extra: (String, String)*): Int = {
		val resp = new HttpBrowser().post(s"/admin/object/create/$objectName", userForm(username, extra*)*)
		assert(location(resp).startsWith(s"/admin/object/$objectName"), s"create failed: ${resp.statusCode()} ${location(resp)}")
		val id = count("SELECT id FROM JakonUser WHERE username = ?", username)
		assert(id > 0)
		id
	}

	test("TestExtUser list page") {
		createThroughAdmin("TestExtUser", unique("extlist"), "someStuff" -> "listed")
		val resp = new HttpBrowser().get("/admin/object/TestExtUser")
		assert(resp.statusCode() == 200)
		assert(resp.body().contains("objectTable"))
	}

	test("TestExtUser is created through admin into parent and child table") {
		val username = unique("extcreate")
		val id = createThroughAdmin("TestExtUser", username, "someStuff" -> "child value")

		assert(queryString("SELECT email FROM JakonUser WHERE id = ?", id) == s"$username@test.cz")
		assert(queryString("SELECT someStuff FROM TestExtUser WHERE id = ?", id) == "child value")
		assert(queryString("SELECT childClass FROM JakonObject WHERE id = ?", id).endsWith("TestExtUser"))
	}

	test("TestExtUser detail shows parent and child fields") {
		val username = unique("extdetail")
		val id = createThroughAdmin("TestExtUser", username, "someStuff" -> "detail value")

		val resp = new HttpBrowser().get(s"/admin/object/TestExtUser/$id")
		assert(resp.statusCode() == 200)
		assert(resp.body().contains(s"$username@test.cz"))
		assert(resp.body().contains("detail value"))
	}

	test("TestExtUser parent and child fields are updated through admin") {
		val username = unique("extupdate")
		val id = createThroughAdmin("TestExtUser", username, "someStuff" -> "before")

		val resp = new HttpBrowser().post(s"/admin/object/TestExtUser/$id",
			userForm(username, "someStuff" -> "after").map {
				case ("lastName", _) => "lastName" -> "Changed"
				case other => other
			}*)
		assert(location(resp).startsWith("/admin/object/TestExtUser"), s"update failed: ${resp.statusCode()}")

		assert(queryString("SELECT lastName FROM JakonUser WHERE id = ?", id) == "Changed")
		assert(queryString("SELECT someStuff FROM TestExtUser WHERE id = ?", id) == "after")
	}

	test("TestExtUser is deleted from parent and child table") {
		val id = createThroughAdmin("TestExtUser", unique("extdelete"), "someStuff" -> "deleted")

		val resp = new HttpBrowser().get(s"/admin/object/delete/TestExtUser/$id")
		assert(location(resp).startsWith("/admin/object/TestExtUser"))

		assert(count("SELECT COUNT(*) FROM JakonObject WHERE id = ?", id) == 0)
		assert(count("SELECT COUNT(*) FROM JakonUser WHERE id = ?", id) == 0)
		assert(count("SELECT COUNT(*) FROM TestExtUser WHERE id = ?", id) == 0)
	}

	test("TestExtNoFields is created, shown, listed and deleted through admin") {
		val username = unique("extnofields")
		val id = createThroughAdmin("TestExtNoFields", username)
		assert(count("SELECT COUNT(*) FROM TestExtNoFields WHERE id = ?", id) == 1)

		val browser = new HttpBrowser
		assert(browser.get("/admin/object/TestExtNoFields").statusCode() == 200)
		val detail = browser.get(s"/admin/object/TestExtNoFields/$id")
		assert(detail.statusCode() == 200)
		assert(detail.body().contains(s"$username@test.cz"))

		browser.get(s"/admin/object/delete/TestExtNoFields/$id")
		assert(count("SELECT COUNT(*) FROM JakonUser WHERE id = ?", id) == 0)
		assert(count("SELECT COUNT(*) FROM TestExtNoFields WHERE id = ?", id) == 0)
	}

	test("TestExtNoFields parent fields are updated through admin") {
		val username = unique("extnoupdate")
		val id = createThroughAdmin("TestExtNoFields", username)

		val resp = new HttpBrowser().post(s"/admin/object/TestExtNoFields/$id",
			userForm(username).map {
				case ("firstName", _) => "firstName" -> "Changed"
				case other => other
			}*)
		assert(location(resp).startsWith("/admin/object/TestExtNoFields"), s"update failed: ${resp.statusCode()}")
		assert(queryString("SELECT firstName FROM JakonUser WHERE id = ?", id) == "Changed")
	}
}
