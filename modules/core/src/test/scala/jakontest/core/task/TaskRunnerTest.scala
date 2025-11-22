package jakontest.core.task

import cz.kamenitxan.jakon.core.task.{FulltextTask, TaskRunner}
import jakontest.test.TestBase
import org.scalatest.DoNotDiscover

@DoNotDiscover
class TaskRunnerTest extends TestBase {

	test("fulltext task start") { _ =>
		val task = new FulltextTask
		task.run()
	}

	test("fulltext task schedule") { _ =>
		val task = new FulltextTask
		TaskRunner.schedule(task)
	}

	test("fulltext task stop") { _ =>
		val task = new FulltextTask
		TaskRunner.stop(task)
	}

}
