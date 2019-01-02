package webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import org.scalatest.FunSuite

/**
  * Created by TPa on 03.09.16.
  */
class AuthTest extends FunSuite{
	val email = "test@gmail.com"
	val password = "paßßw0rd"

	test("create user") {
		val user = new JakonUser()
		user.firstName = "testName"
		user.lastName = "lastName"
		user.email = email
		user.password = password

		assert(user.create() > 0)
	}

	test("check password") {
		val sql = "SELECT id, username, password, enabled, acl_id FROM JakonUser WHERE email = ?"
		val stmt = DBHelper.getPreparedStatement(sql)
		stmt.setString(1, email)
		val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
		val user = result.entity.asInstanceOf[JakonUser]


		assert(Authentication.checkPassword(password, user.password))
	}
}
