package cz.kamenitxan.jakon

import java.io.IOException
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, DeployMode, Page, Post}
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.task.{RenderTask, TaskRunner}
import cz.kamenitxan.jakon.devtools.DevRender
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controler.impl.{DeployControler, TaskController}
import org.slf4j.LoggerFactory
import spark.{Filter, Request, Response}
import spark.Spark.{afterAfter, before, port, staticFiles}

class JakonInit {
	private val logger = LoggerFactory.getLogger(this.getClass)

	var daoSetup = () => {
		//DBHelper.addDao(classOf[Post])
		//DBHelper.addDao(classOf[Page])
		//DBHelper.addDao(classOf[Category])
	}

	var routesSetup = () => {}

	def adminControllers() {
		AdminSettings.registerCustomController(new DeployControler().getClass)
		AdminSettings.registerCustomController(classOf[TaskController])
	}

	def taskSetup(): Unit = {
		TaskRunner.registerTask(new RenderTask(10, TimeUnit.MINUTES))
	}

	def run(): Unit = {
		staticFiles.externalLocation(Settings.getStaticDir)
		port(Settings.getPort)

		try
			Settings.init(null)
		catch {
			case e: IOException => logger.error("Jdufanit: cant load jakon settings", e)
		}
		logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()
		adminControllers()
		Director.start()

		afterAfter((_: Request, _: Response) => PageContext.destroy())
		if (Settings.getDeployMode == DeployMode.DEVEL) {
			before((request: Request, _: Response) => {
					DevRender.rerender(request.pathInfo())
				}
			)
		}
		routesSetup()
		taskSetup()
	}
}
