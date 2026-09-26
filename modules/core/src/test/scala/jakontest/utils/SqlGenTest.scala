package jakontest.utils

import cz.kamenitxan.jakon.core.database.{DBHelper, DBInitializer}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.SqlGen
import jakontest.test.TestBase
import jakontest.utils.entity.TestObject
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
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser")
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
