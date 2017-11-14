package webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.controler.Authentication
import org.scalatest.FunSuite

/**
  * Created by TPa on 03.09.16.
  */
class AuthTest extends FunSuite{

	test("create user") {
		val user = new JakonUser()
		user.firstName = "testName"
		user.lastName = "lastName"
		user.email = "test@gmail.com"
		user.password = "paßßw0rd"

		assert(Authentication.createUser(user) != null)
	}

	test("check password") {
		/*val user = DBHelper.getUserDao.queryForId(1)
		assert(Authentication.checkPassword("paßßw0rd", user.password))*/
	}
}
