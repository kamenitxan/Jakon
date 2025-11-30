package jakontest.core

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.*
import cz.kamenitxan.jakon.core.service.UserService
import jakontest.test.TestBase
import jakontest.utils.entity.{TestEmbeddedObject, TestObject}
import org.scalatest.DoNotDiscover

import java.util.Date
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

	var eoid = 0
	test("Create TestEmbeddedObject") { _ =>
		val obj = new TestObject
		obj.string = "TestEmbeddedObject"
		val embedded = new TestEmbeddedObject
		embedded.int = eoid
		embedded.string = "42"
		obj.embedded = embedded
		eoid = obj.create()
	}

	test("Fetch TestEmbeddedObject") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val sql = "SELECT * From TestObject WHERE id = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, eoid)
			val entity = DBHelper.selectSingleDeep(stmt)(conn, classOf[TestObject])
			assert(entity.embedded != null)
		})
	}

	var otmid = 0
	test("Create OneToMany") { _ =>
		val users = DBHelper.withDbConnection(implicit conn => {
			UserService.getAllUsers()
		}).take(2)

		val obj = new TestObject
		obj.oneToMany = users
		eoid = obj.create()
	}

	test("Fetch OneToMany") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val sql = "SELECT * From TestObject WHERE id = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, eoid)
			val entity = DBHelper.selectSingleDeep(stmt)(conn, classOf[TestObject])
			assert(entity.oneToMany.nonEmpty)
		})
	}

	test("Create empty OneToMany") { _ =>
		val obj = new TestObject
		obj.oneToMany = Seq.empty
		eoid = obj.create()
	}

	test("Fetch empty OneToMany") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val sql = "SELECT * From TestObject WHERE id = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setInt(1, eoid)
			val entity = DBHelper.selectSingleDeep(stmt)(conn, classOf[TestObject])
			assert(entity.oneToMany.isEmpty)
		})
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
