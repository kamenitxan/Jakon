package core.task

import cz.kamenitxan.jakon.core.task.{FulltextTask, TaskRunner}
import test.TestBase

class TaskRunnerTest extends TestBase {

	test("fulltext task") { _ =>
		val task = new FulltextTask
		task.run()
	}

}
