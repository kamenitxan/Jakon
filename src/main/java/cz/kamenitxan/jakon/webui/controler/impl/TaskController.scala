package cz.kamenitxan.jakon.webui.controler.impl

import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controler.{AbstractController, ExecuteFun}
import spark.route.HttpMethod
import spark.{Request, Response}

import scala.collection.JavaConverters._

/**
  * Created by TPa on 27.05.18.
  */
class TaskController extends AbstractController {
	override val template: String = "pages/task"
	override val icon: String = "fa-tasks"

	override def name(): String = "Tasks"

	override def path(): String = "task"

	override def render(req: Request, res: Response): Context = {
		new Context(Map[String, Any](
			"tasks" -> TaskRunner.taskList.asJava
		), template)
	}

	@ExecuteFun(path = "task/run/:name", method = HttpMethod.get)
	def runSingle(req: Request, res: Response): Context = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.equals(name))
		if (task.isDefined) TaskRunner.runSingle(task.get)
		res.redirect("/admin/task")
		new Context(Map[String, Any](), template)
	}
}
