package jakontest.webui

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser, KeyValueEntity}
import cz.kamenitxan.jakon.utils.security.CsrfProtection
import jakontest.test.HttpBrowser
import jakontest.test.HttpBrowser.location
import jakontest.utils.entity.{TestM2MOwner, TestM2MTag, TestObject}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.util.UUID

/** Admin object CRUD edge cases, authorization, login and CSRF. */
@DoNotDiscover
class AdminAccessTest extends AnyFunSuite {

	private val Password = "password123"

	private def unique(prefix: String) = prefix + UUID.randomUUID().toString.take(8)

	private def queryInt(sql: String, param: Any): Int = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, String.valueOf(param))
		val rs = stmt.executeQuery()
		val res = if (rs.next()) rs.getInt(1) else 0
		stmt.close()
		res
	})

	private def createUser(adminAllowed: Boolean, enabled: Boolean = true, masterAdmin: Boolean = false): JakonUser = {
		val acl = new AclRule
		acl.name = unique("acl")
		acl.adminAllowed = adminAllowed
		acl.masterAdmin = masterAdmin
		acl.create()

		val user = new JakonUser
		user.username = unique("user")
		user.email = user.username + "@example.com"
		user.password = Password
		user.enabled = enabled
		user.acl = acl
		user.create()
		user
	}

	private def createTag(name: String): TestM2MTag = {
		val tag = new TestM2MTag
		tag.name = name
		tag.create()
		tag
	}

	private def tagIds(ownerId: Int): Set[Int] = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement("SELECT testM2MTag_id FROM TestM2MOwnerTestM2MTag WHERE testM2MOwner_id = ?")
		stmt.setInt(1, ownerId)
		val rs = stmt.executeQuery()
		val ids = Iterator.continually(rs).takeWhile(_.next()).map(_.getInt(1)).toSet
		stmt.close()
		ids
	})

	test("ManyToMany relation is saved, loaded and updated through admin") {
		val browser = new HttpBrowser
		val red = createTag("red")
		val blue = createTag("blue")
		val name = unique("owner")

		val created = browser.post("/admin/object/create/TestM2MOwner?save_and_new=true",
			"name" -> name, "tags" -> red.id.toString, "tags" -> blue.id.toString)
		assert(location(created).startsWith("/admin/object/create/TestM2MOwner"))
		val ownerId = queryInt("SELECT id FROM TestM2MOwner WHERE name = ?", name)
		assert(ownerId > 0)
		assert(tagIds(ownerId) == Set(red.id, blue.id))

		// relation is loaded with the owner
		val loaded = DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonObject JOIN TestM2MOwner ON TestM2MOwner.id = JakonObject.id WHERE TestM2MOwner.id = ?")
			stmt.setInt(1, ownerId)
			DBHelper.selectSingleDeep(stmt)(conn, classOf[TestM2MOwner])
		})
		assert(loaded.tags.map(_.name).toSet == Set("red", "blue"))

		assert(browser.get(s"/admin/object/TestM2MOwner/$ownerId").statusCode() == 200)
		assert(browser.get("/admin/object/TestM2MOwner").statusCode() == 200)

		val updated = browser.post(s"/admin/object/TestM2MOwner/$ownerId", "name" -> name, "tags" -> blue.id.toString)
		assert(location(updated).startsWith("/admin/object/TestM2MOwner"))
		assert(tagIds(ownerId) == Set(blue.id))
	}

	test("object referenced by KeyValueEntity can not be deleted") {
		val browser = new HttpBrowser
		val obj = new TestObject
		obj.create()
		val kv = new KeyValueEntity
		kv.name = unique("kv")
		kv.value = obj.id.toString
		kv.create()

		val resp = browser.get(s"/admin/object/delete/TestObject/${obj.id}")
		assert(location(resp).startsWith("/admin/object/TestObject"))
		assert(queryInt("SELECT COUNT(*) FROM JakonObject WHERE id = ?", obj.id) == 1)
	}

	test("creating object violating UNIQUE constraint redirects back to create form") {
		val browser = new HttpBrowser
		val name = unique("kv")
		assert(location(browser.post("/admin/object/create/KeyValueEntity", "name" -> name, "value" -> "1")).startsWith("/admin/object/KeyValueEntity"))
		val resp = browser.post("/admin/object/create/KeyValueEntity", "name" -> name, "value" -> "2")
		assert(location(resp).startsWith("/admin/object/create/KeyValueEntity"))
		assert(queryInt("SELECT COUNT(*) FROM KeyValueEntity WHERE name = ?", name) == 1)
	}

	test("unknown object returns 404") {
		val browser = new HttpBrowser
		assert(browser.get("/admin/object/NonExistentObject").statusCode() == 404)
		assert(browser.get("/admin/object/create/NonExistentObject").statusCode() == 404)
	}

	test("admin user without access to object controller gets unauthorized page") {
		val user = createUser(adminAllowed = true)
		val browser = new HttpBrowser
		assert(location(browser.login(user.email, Password)).startsWith("/admin/index"))

		val obj = new TestObject
		obj.create()
		val unauthorized = "You are not permitted to access TestObject"
		assert(browser.get("/admin/object/TestObject").body().contains(unauthorized))
		assert(browser.get("/admin/object/create/TestObject").body().contains(unauthorized))
		assert(browser.get(s"/admin/object/TestObject/${obj.id}").body().contains(unauthorized))
		assert(browser.post(s"/admin/object/TestObject/${obj.id}", "string" -> "changed").body().contains(unauthorized))
		assert(browser.get(s"/admin/object/delete/TestObject/${obj.id}").body().contains(unauthorized))
		assert(queryInt("SELECT COUNT(*) FROM JakonObject WHERE id = ?", obj.id) == 1)
	}

	test("login of unknown, disabled and non admin user does not enter administration") {
		val browser = new HttpBrowser
		assert(location(browser.login("nobody@example.com", Password)).isEmpty)

		val disabled = createUser(adminAllowed = true, enabled = false)
		assert(location(browser.login(disabled.email, Password)).isEmpty)

		val nonAdmin = createUser(adminAllowed = false)
		assert(location(browser.login(nonAdmin.email, Password)).isEmpty)
	}

	test("account is locked after too many wrong passwords") {
		val user = createUser(adminAllowed = true)
		val browser = new HttpBrowser
		(1 to 5).foreach(_ => assert(location(browser.login(user.email, "wrong")).isEmpty))
		// correct password is refused while account is locked
		assert(location(browser.login(user.email, Password)).isEmpty)
	}

	test("CSRF token is required for admin POST of logged in user outside DEVEL mode") {
		val user = createUser(adminAllowed = true, masterAdmin = true)
		val browser = new HttpBrowser
		assert(location(browser.login(user.email, Password)).startsWith("/admin/index"))

		val original = Settings.getDeployMode
		Settings.setDeployMode(DeployMode.PRODUCTION)
		try {
			val forbidden = browser.post("/admin/object/create/KeyValueEntity", "name" -> unique("kv"), "value" -> "1")
			assert(forbidden.statusCode() == 403)

			// token is rendered into admin pages
			val page = browser.get("/admin/object/create/KeyValueEntity").body()
			val token = "name=\"_csrf\" value=\"([^\"]+)\"".r.findFirstMatchIn(page).map(_.group(1))
			assert(token.isDefined)
			val name = unique("kv")
			val allowed = browser.post("/admin/object/create/KeyValueEntity", CsrfProtection.FormParam -> token.get, "name" -> name, "value" -> "1")
			assert(allowed.statusCode() == 302)
			assert(queryInt("SELECT COUNT(*) FROM KeyValueEntity WHERE name = ?", name) == 1)

			// logged in admin opening login page is redirected to dashboard
			assert(location(browser.get("/admin")).startsWith("/admin/index"))
		} finally {
			Settings.setDeployMode(original)
		}
	}
}
