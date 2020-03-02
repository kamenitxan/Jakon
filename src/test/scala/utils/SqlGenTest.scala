package utils

import cz.kamenitxan.jakon.core.database.{DBHelper, DBInitializer}
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.utils.SqlGen
import test.TestBase
import utils.entity.TestObject

class SqlGenTest extends TestBase {

	test("insertStmt") { _ =>
	  val obj = new JakonUser()
		val tc = SqlGen.insertStmt(obj, DBHelper.getConnection, 0)
		assert(tc != null)
	}

	test("updateStmt") { _ =>
		val obj = new JakonUser()
		val tc = SqlGen.updateStmt(obj, DBHelper.getConnection, 0)
		assert(tc != null)
	}

	test("insertStmt with TestObject") { _ =>
		val obj = new TestObject()
		val tc = SqlGen.insertStmt(obj, DBHelper.getConnection, 0)
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
		DBHelper.withDbConnection(conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser")
			val users = DBHelper.select(stmt, classOf[JakonUser])
			assert(users.nonEmpty)
		})
	}

	/*test("DBHelper selectDeep") { _ =>
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement("SELECT * FROM JakonUser")
			val users = DBHelper.selectDeep(stmt, classOf[JakonUser])
			assert(users.nonEmpty)
			assert(users.forall( u => u.acl != null))
		})
	}*/
}
