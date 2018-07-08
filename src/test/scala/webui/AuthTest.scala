package webui

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import org.hibernate.criterion.Restrictions
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

		assert(Authentication.createUser(user) != null)
	}

	test("check password") {
		val ses = DBHelper.getSession
		ses.beginTransaction()
		val criteria = getSession.createCriteria(classOf[JakonUser])
		val user = criteria.add(Restrictions.eq("email", email)).uniqueResult().asInstanceOf[JakonUser]
		ses.getTransaction.commit()
		assert(Authentication.checkPassword(password, user.password))
	}
}
