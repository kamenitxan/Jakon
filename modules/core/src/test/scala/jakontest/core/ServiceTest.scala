package jakontest.core

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.{JakonFileService, UserService}
import jakontest.test.TestBase
import org.scalatest.DoNotDiscover

/**
  * Created by TPa on 08/04/2021.
  */
@DoNotDiscover
class ServiceTest extends TestBase {

	var user: JakonUser = _
	test("UserService getAllUsers") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val users = UserService.getAllUsers()
			assert(users.nonEmpty)
			user = users.head
		})
	}

	test("UserService getById") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val u = UserService.getById(user.id)
			assert(u != null)
		})
	}

	test("UserService getByEmail") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val u = UserService.getByEmail(user.email)
			assert(u != null)
		})
	}

	test("UserService getByUsername") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val u = UserService.getByUsername(user.username)
			assert(u != null)
		})
	}

	test("JakonFileService getImages") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val images = JakonFileService.getImages()
			assert(images != null)
		})
	}

}
