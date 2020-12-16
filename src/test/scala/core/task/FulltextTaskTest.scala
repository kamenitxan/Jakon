package core.task

import cz.kamenitxan.jakon.core.task.FulltextTask
import test.TestBase

class FulltextTaskTest extends TestBase {

	test("fulltext task") { _ =>
		val task = new FulltextTask
		task.run()
	}

}
