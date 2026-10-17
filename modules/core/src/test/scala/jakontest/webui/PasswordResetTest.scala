package jakontest.webui

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.security.AuthUtils
import jakontest.test.HttpBrowser
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Forgotten password - both steps. */
@DoNotDiscover
class PasswordResetTest extends AnyFunSuite {

	private def createUser(): JakonUser = {
		val user = new JakonUser
		user.username = "reset" + UUID.randomUUID().toString.take(8)
		// example.com is rejected by email validator
		user.email = user.username + "@test.cz"
		user.password = "oldPassword"
		user.enabled = true
		user.create()
		user
	}

	/** Token as it is sent in reset link, stored token is url encoded. */
	private def resetToken(userId: Int): Option[String] = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement("SELECT token FROM ResetPasswordEmailEntity WHERE user = ?")
		stmt.setInt(1, userId)
		val rs = stmt.executeQuery()
		val token = if (rs.next()) Option(rs.getString(1)) else None
		stmt.close()
		token.map(t => URLDecoder.decode(t, StandardCharsets.UTF_8))
	})

	private def passwordHash(userId: Int): String = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement("SELECT password FROM JakonUser WHERE id = ?")
		stmt.setInt(1, userId)
		val rs = stmt.executeQuery()
		rs.next()
		val hash = rs.getString(1)
		stmt.close()
		hash
	})

	test("password is changed with valid reset token") {
		val user = createUser()
		val browser = new HttpBrowser
		browser.post("/admin/resetPassword", "email" -> user.email)
		val token = resetToken(user.id)
		assert(token.isDefined)

		val resp = browser.post("/admin/setPassword", "token" -> token.get, "password" -> "newPassword", "password_check" -> "newPassword")
		assert(resp.statusCode() == 302)
		assert(AuthUtils.checkPassword("newPassword", passwordHash(user.id)))
		// token can be used only once
		assert(resetToken(user.id).isEmpty)
	}

	test("password is not changed with unknown reset token") {
		val user = createUser()
		val hash = passwordHash(user.id)
		new HttpBrowser().post("/admin/setPassword", "token" -> "unknown-token", "password" -> "newPassword", "password_check" -> "newPassword")
		assert(passwordHash(user.id) == hash)
	}

	test("reset of unknown email does nothing") {
		val resp = new HttpBrowser().post("/admin/resetPassword", "email" -> "nobody@example.com")
		assert(resp.statusCode() == 302)
	}
}
