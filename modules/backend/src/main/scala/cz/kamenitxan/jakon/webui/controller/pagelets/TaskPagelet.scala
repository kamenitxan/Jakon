package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.core.model.KeyValueEntity
import cz.kamenitxan.jakon.core.service.KeyValueService
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.logging.Logger
import spark.{Request, Response}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
 * Created by TPa on 19.03.2022.
 */
@Pagelet(path = "/admin/task", showInAdmin = true)
class TaskPagelet extends AbstractAdminPagelet {

	override val name: String = this.getClass.getSimpleName

	override val icon: String = "fa-tasks"

	private val redirectPath = "/admin/task"

	@Get(path = "", template = "pagelet/task")
	def render(req: Request, res: Response): mutable.Map[String, Any] = {
		mutable.Map[String, Any](
			"tasks" -> TaskRunner.taskList.asJava
		)
	}

	@Get(path = "/run/:name", template = "pagelet/task")
	def runSingle(req: Request, res: Response): Unit = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) TaskRunner.runSingle(task.get)
		res.redirect(redirectPath)
	}

	@Get(path = "/pause/:name", template = "pagelet/task")
	def pause(req: Request, res: Response): Unit = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) {
			val kve = new KeyValueEntity
			kve.name = name + "_disabled"
			kve.value = "disabled"
			kve.create()
			Logger.info(s"Task $name paused")
		}
		res.redirect(redirectPath)
	}

	@Get(path = "/resume/:name", template = "pagelet/task")
	def resume(req: Request, res: Response): Unit = {
		val name = req.params(":name")
		val task = TaskRunner.taskList.find(t => t.name.value.equals(name))
		if (task.isDefined) {
			DBHelper.withDbConnection(implicit conn => {
				KeyValueService.deleteByKey(name + "_disabled")
				TaskRunner.schedule(task.get)
				Logger.info(s"Task $name resumed")
			})
		}
		res.redirect(redirectPath)
	}

}
