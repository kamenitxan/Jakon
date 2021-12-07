package core

import java.util.Date
import cz.kamenitxan.jakon.core.model._
import org.scalatest.DoNotDiscover
import test.TestBase
import utils.entity.{TestEmbeddedObject, TestObject}

import scala.util.Random

@DoNotDiscover
class ModelTest extends TestBase {

	test("Page") { _ =>
		val page = new Page()
		page.content = "test"

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
		obj.update()
	}

	test("JakonObject toJson") { _ =>
		val obj = new BasicJakonObject
		obj.id = 42
		val json = obj.toJson
		assertNotEmpty(json)
		assert(json.contains("42"))
	}

	test("AclRule") { _ =>
		val obj = new AclRule()
		obj.name = "test"
		obj.masterAdmin = false
		obj.adminAllowed = false
		obj.create()
		obj.update()
	}

	test("KeyValueEntity") { _ =>
		val obj = new KeyValueEntity()
		obj.name = "test" + Random.nextInt()
		obj.value = "test"
		obj.create()
		obj.update()
		obj.toString
	}

	test("TestEmbeddedObject") { _ =>
		val obj = new TestObject
		val embedded = new TestEmbeddedObject
		embedded.int = 42
		embedded.string = "42"
		obj.embedded = embedded
		obj.create()
	}

	var post: Post = _
	test("Post Create") { _ =>
		val obj = new Post {
			date = new Date()
			perex = "perex"
			title = "title"
			content = "content"
		}
		post = obj
		post.create()
	}

	test("Post Update") { _ =>
		post.title = "title2"
		post.update()
	}
}
