package jakon

import cz.kamenitxan.jakon.core.model.{Category, EmailConfirmation, Page}
import test.TestBase

class ModelTest extends TestBase {

	test("Page") { _ =>
		val page = new Page()
		page.setContent("test")

		val order = 1
		page.setObjectOrder(order)
		assert(order == page.getObjectOrder)

		val id = page.create()
		page.title = "Test"
		page.update()
	}

	test("EmailConfirmation") { _=>
		val entity = new EmailConfirmation()
		assert(entity != null)
	}

	test("Category") { _=>
		val entity = new Category()
		assert(entity != null)

		val order = 1
		entity.setObjectOrder(order)
		assert(order == entity.getObjectOrder)

		assert(entity.toString != null)
	}
}
