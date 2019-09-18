package jakon

import cz.kamenitxan.jakon.core.model.{AclRule, BasicJakonObject, Category, EmailConfirmation, Page}
import test.TestBase

class ModelTest extends TestBase {

	test("Page") { _ =>
		val page = new Page()
		page.setContent("test")

		val order = 1
		page.objectOrder = order
		assert(order == page.objectOrder)

		val id = page.create()
		page.title = "Test"
		page.update()

		page.delete()
	}

	test("EmailConfirmation") { _=>
		val entity = new EmailConfirmation()
		assert(entity != null)
	}

	test("Category") { _=>
		val entity = new Category()
		assert(entity != null)

		val order = 1
		entity.objectOrder = order
		assert(order == entity.objectOrder)

		assert(entity.toString != null)
	}

	test("JakonObject") { _ =>
		val obj = new BasicJakonObject
		//assertNotEmpty(obj.toJson)
		assertNotEmpty(obj.toString)
		obj.objectOrder = 2.0
		assert(2.0 == obj.objectOrder)
	}

	test("AclRule") { _ =>
		val obj = new AclRule()
		obj.name = "test"
		obj.masterAdmin = false
		obj.adminAllowed = false
		obj.create()
		obj.update()
	}
}
