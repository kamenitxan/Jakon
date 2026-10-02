package jakontest.core.database

import cz.kamenitxan.jakon.core.database.{DBHelper, DBInitializer}
import cz.kamenitxan.jakon.logging.{Error, LogService}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime

@DoNotDiscover
class DBInitializerTest extends AnyFunSuite {

	/**
	  * Runs consistency check and returns messages of errors logged during it.
	  */
	private def consistencyErrors(): Seq[String] = {
		val start = LocalDateTime.now()
		DBInitializer.checkDbConsistency()
		LogService.getLogs
			.filter(l => l.severity == Error && !l.time.isBefore(start))
			.map(_.message)
	}

	private def execute(sql: String): Unit = {
		DBHelper.withDbConnection(conn => {
			val stmt = conn.createStatement()
			try {
				stmt.execute(sql)
			} finally {
				stmt.close()
			}
		})
	}

	test("consistent DB with i18n object reports no i18n errors") {
		val errors = consistencyErrors()
		assert(!errors.exists(_.contains("TestObjectI18n")), errors)
		assert(!errors.exists(_.contains("TestObject.i18n")), errors)
	}

	test("i18n field is not reported as missing column") {
		val errors = consistencyErrors()
		assert(!errors.contains("Field TestObject.i18n is not in DB"))
	}

	test("missing i18n table is reported") {
		execute("ALTER TABLE TestObjectI18n RENAME TO TestObjectI18n_tmp")
		try {
			val errors = consistencyErrors()
			assert(errors.contains("I18n table TestObjectI18n for TestObject is not in DB"), errors)
			// columns can not be checked when the table is missing
			assert(!errors.exists(_.startsWith("Field TestObjectI18n.")), errors)
		} finally {
			execute("ALTER TABLE TestObjectI18n_tmp RENAME TO TestObjectI18n")
		}
	}

	test("missing column in i18n table is reported") {
		execute("ALTER TABLE TestObjectI18n DROP COLUMN description")
		try {
			val errors = consistencyErrors()
			assert(errors.contains("Field TestObjectI18n.description is not in DB"), errors)
			assert(!errors.contains("Field TestObjectI18n.name is not in DB"), errors)
			assert(!errors.exists(_.startsWith("I18n table")), errors)
		} finally {
			execute("ALTER TABLE TestObjectI18n ADD COLUMN description VARCHAR(100)")
		}
	}

	test("DB is consistent again after restoring i18n table") {
		val errors = consistencyErrors()
		assert(!errors.exists(_.contains("TestObjectI18n")), errors)
	}
}
