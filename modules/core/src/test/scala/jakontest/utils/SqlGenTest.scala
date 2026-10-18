package jakontest.utils

import cz.kamenitxan.jakon.core.database.{DBHelper, DBInitializer}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.service.UserService
import cz.kamenitxan.jakon.utils.SqlGen
import jakontest.test.TestBase
import jakontest.utils.entity.{TestExtUser, TestFirstFk, TestObject, TestStandaloneFk}
import org.scalatest.DoNotDiscover

import scala.collection.mutable

@DoNotDiscover
class SqlGenTest extends TestBase {

	test("insertStmt") { _ =>
		val obj = new JakonUser()
		val tc = SqlGen.insertStmt(obj, DBHelper.getConnection, 0, false)
		assert(tc != null)
	}

	test("updateStmt") { _ =>
		val obj = new JakonUser()
		val tc = SqlGen.updateStmt(obj, DBHelper.getConnection, 0)
		assert(tc != null)
	}

	test("insertStmt with TestObject") { _ =>
		val obj = new TestObject()
		val tc = SqlGen.insertStmt(obj, DBHelper.getConnection, 0, false)
		assert(tc != null)
	}

	test("updateStmt with TestObject") { _ =>
		val obj = new TestObject()
		val tc = SqlGen.updateStmt(obj, DBHelper.getConnection, 0)
		assert(tc != null)
	}

	private def queryInt(sql: String, id: Int): Option[Int] = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, id)
		val rs = stmt.executeQuery()
		val res = if (rs.next()) Option(rs.getObject(1)).map(_.toString.toInt) else None
		stmt.close()
		res
	})

	private def queryString(sql: String, id: Int): String = DBHelper.withDbConnection(conn => {
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, id)
		val rs = stmt.executeQuery()
		val res = if (rs.next()) rs.getString(1) else null
		stmt.close()
		res
	})

	private def masterAdmin: JakonUser = DBHelper.withDbConnection(implicit conn => UserService.getMasterAdmin())

	test("insert stores foreign keys of ManyToOne fields") { _ =>
		val other = new TestObject()
		other.create()

		val obj = new TestObject()
		obj.user = masterAdmin
		obj.self = other
		obj.create()

		assert(queryInt("SELECT user_id FROM TestObject WHERE id = ?", obj.id).contains(masterAdmin.id))
		assert(queryInt("SELECT self_id FROM TestObject WHERE id = ?", obj.id).contains(other.id))
	}

	test("insert stores null foreign keys of empty ManyToOne fields") { _ =>
		val obj = new TestObject()
		obj.create()

		assert(queryInt("SELECT user_id FROM TestObject WHERE id = ?", obj.id).isEmpty)
		assert(queryInt("SELECT self_id FROM TestObject WHERE id = ?", obj.id).isEmpty)
	}

	test("update changes and clears foreign keys") { _ =>
		val first = new TestObject()
		first.create()
		val second = new TestObject()
		second.create()

		val obj = new TestObject()
		obj.self = first
		obj.create()

		// BUG: updateStmt binds WHERE id to parameter annotatedFields.length + 1, but @Embedded field of TestObject
		// takes more parameters - id overwrites last column and WHERE id stays null, so nothing is updated
		pendingUntilFixed {
			obj.self = second
			obj.user = masterAdmin
			obj.update()
			assert(queryInt("SELECT self_id FROM TestObject WHERE id = ?", obj.id).contains(second.id))
			assert(queryInt("SELECT user_id FROM TestObject WHERE id = ?", obj.id).contains(masterAdmin.id))

			obj.self = null
			obj.user = null
			obj.update()
			assert(queryInt("SELECT self_id FROM TestObject WHERE id = ?", obj.id).isEmpty)
			assert(queryInt("SELECT user_id FROM TestObject WHERE id = ?", obj.id).isEmpty)
		}
	}

	test("standalone object with foreign key is inserted and updated") { _ =>
		val obj = new TestStandaloneFk()
		obj.name = "standalone"
		obj.owner = masterAdmin
		val id = obj.create()
		assert(id > 0)
		assert(queryString("SELECT name FROM TestStandaloneFk WHERE id = ?", id) == "standalone")
		assert(queryInt("SELECT owner_id FROM TestStandaloneFk WHERE id = ?", id).contains(masterAdmin.id))
		// standalone object has no JakonObject row
		assert(queryInt("SELECT id FROM JakonObject WHERE id = ? AND childClass LIKE '%TestStandaloneFk'", id).isEmpty)

		obj.name = "updated"
		obj.owner = null
		obj.update()
		assert(queryString("SELECT name FROM TestStandaloneFk WHERE id = ?", id) == "updated")
		assert(queryInt("SELECT owner_id FROM TestStandaloneFk WHERE id = ?", id).isEmpty)
	}

	test("first field with foreign key to inherited object is stored to _id column") { _ =>
		val ext = new TestExtUser()
		ext.username = "firstfk" + System.nanoTime()
		ext.email = ext.username + "@test.cz"
		ext.password = "password"
		ext.create()

		val obj = new TestFirstFk()
		obj.ext = ext
		obj.name = "first fk"
		// BUG: SqlGen checks only direct JakonObject parent for the first field, TestExtUser extends JakonUser,
		// so column "ext" is used instead of "ext_id"
		pendingUntilFixed {
			obj.create()
			assert(queryInt("SELECT ext_id FROM TestFirstFk WHERE id = ?", obj.id).contains(ext.id))
		}
	}

	test("second DBInitialization") { _ =>
		try {
			DBInitializer.createTables()
			DBInitializer.checkDbConsistency()
		} catch {
			case ex: Throwable => fail(ex)
		}
	}

	test("DBHelper select") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser")
			val users = DBHelper.select(stmt, classOf[JakonUser])
			assert(users.nonEmpty)
		})
	}

	test("DBHelper selectDeep") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			// registered users do not have ACL
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser WHERE acl_id IS NOT NULL")
			val users = DBHelper.selectDeep(stmt)(conn, classOf[JakonUser])
			assert(users.nonEmpty)
			assert(users.forall( u => u.acl != null))
		})
	}

	test("parseFilterParams invalid number") { _ =>
		val params = mutable.Map(
			"double" -> "invalid"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert("WHERE TestObject.double = ?" == res.sql)
		assert(Seq("invalid") == res.params)
	}

	test("parseFilterParams invalid boolean") { _ =>
		val params = mutable.Map(
			"boolean" -> "invalid"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert("WHERE TestObject.boolean = ?" == res.sql)
		assert(Seq("invalid") == res.params)
	}

	test("parseFilterParams valid number and boolean") { _ =>
		val params = mutable.LinkedHashMap(
			"double" -> "1.5",
			"boolean" -> "true"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert("WHERE TestObject.double = ? AND TestObject.boolean = ?" == res.sql)
		assert(Seq(1.5, 1) == res.params)
	}

	test("parseFilterParams injection is parametrized") { _ =>
		val params = mutable.Map(
			"string" -> "x\" OR 1=1 --"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert("WHERE TestObject.string = ?" == res.sql)
		assert(Seq("x\" or 1=1 --") == res.params)
	}

	test("parseFilterParams like") { _ =>
		val params = mutable.Map(
			"string" -> "Ab*"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert("WHERE TestObject.string LIKE ?" == res.sql)
		assert(Seq("ab%") == res.params)
	}

	test("parseFilterParams unknown field is ignored") { _ =>
		val params = mutable.Map(
			"nonexistent\" OR 1=1 --" -> "x"
		)
		val res = SqlGen.parseFilterParams(params, classOf[TestObject])
		assert(res.isEmpty)
	}
}
