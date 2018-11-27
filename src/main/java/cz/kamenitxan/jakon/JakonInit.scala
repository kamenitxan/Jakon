package cz.kamenitxan.jakon

import java.io.{File, IOException}
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.{AnnotationScanner, DeployMode, SettingValue, Settings}
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.task.{FulltextTask, RenderTask, TaskRunner}
import cz.kamenitxan.jakon.devtools.{DevRender, StaticFilesController}
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.controler.impl.{DeployControler, TaskController}
import cz.kamenitxan.jakon.webui.entity.ResetPasswordEmailEntity
import org.slf4j.LoggerFactory
import spark.Spark._
import spark.debug.DebugScreen.enableDebugScreen
import spark.{Request, Response}


class JakonInit {
	private val logger = LoggerFactory.getLogger(this.getClass)

	var daoSetup: () => Unit = () => {
		//DBHelper.addDao(classOf[Post])
		//DBHelper.addDao(classOf[Page])
		//DBHelper.addDao(classOf[Category])
	}

	var routesSetup: () => Unit = () => {}

	def adminControllers() {
		AdminSettings.registerCustomController(new DeployControler().getClass)
		AdminSettings.registerCustomController(classOf[TaskController])
	}

	def taskSetup(): Unit = {
		TaskRunner.registerTask(new RenderTask(10, TimeUnit.MINUTES))
		TaskRunner.registerTask(new FulltextTask)
		if (Settings.isEmailEnabled) {
			DBHelper.addDao(classOf[EmailEntity])
			DBHelper.addDao(classOf[EmailTemplateEntity])
			DBHelper.addDao(classOf[ResetPasswordEmailEntity])
			TaskRunner.registerTask(new EmailSendTask(1, TimeUnit.MINUTES))
		}

	}

	def run(): Unit = {
		staticFiles.externalLocation(Settings.getStaticDir)
		port(Settings.getPort)

		logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()
		adminControllers()
		taskSetup()
		Director.start()

		afterAfter((_: Request, _: Response) => PageContext.destroy())
		if (Settings.getDeployMode == DeployMode.DEVEL) {
			before((request: Request, _: Response) => {
					DevRender.rerender(request.pathInfo())
				}
			)
			enableDebugScreen()

			notFound((req: Request, res: Response) => new StaticFilesController().doGet(req, res))
		}
		routesSetup()
		AnnotationScanner.scan()
		Director.render()

	}
}
