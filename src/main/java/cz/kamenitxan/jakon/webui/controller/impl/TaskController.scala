package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.KeyValueEntity
import cz.kamenitxan.jakon.core.service.KeyValueService
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controller.{AbstractController, ExecuteFun}
import spark.route.HttpMethod
import spark.{Request, Response}

import scala.jdk.CollectionConverters._

/**
  * Created by TPa on 27.05.18.
  */
class TaskController extends AbstractController {
	override val template: String = "pages/task"
	override val icon: String = "fa-tasks"

	override def name(): String = "TASKS"

	override def path(): String = "task"

	override def render(req: Request, res: Response): Context = {
		new Context(Map[String, Any](
			"tasks" -> TaskRunner.taskList.asJava
		), template)
	}

	@ExecuteFun(path = "task/run/:name", method = HttpMethod.get)
	def runSingle(req: Request, res: Response): Context = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) TaskRunner.runSingle(task.get)
		res.redirect("/admin/task")
		new Context(Map[String, Any](), template)
	}

	@ExecuteFun(path = "task/pause/:name", method = HttpMethod.get)
	def pause(req: Request, res: Response): Context = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) {
			val kve = new KeyValueEntity
			kve.name = name + "_disabled"
			kve.value = "disabled"
			kve.create()
			Logger.info(s"Task $name paused")
		}
		res.redirect("/admin/task")
		new Context(Map[String, Any](), template)
	}

	@ExecuteFun(path = "task/resume/:name", method = HttpMethod.get)
	def resume(req: Request, res: Response): Context = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) {
			DBHelper.withDbConnection(implicit conn => {
				KeyValueService.deleteByKey(name + "_disabled")
				TaskRunner.schedule(task.get)
				Logger.info(s"Task $name resumed")
			})
		}
		res.redirect("/admin/task")
		new Context(Map[String, Any](), template)
	}
}
