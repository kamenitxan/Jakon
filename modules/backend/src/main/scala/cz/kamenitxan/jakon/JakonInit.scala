package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.{AnnotationScanner, ConfigurationInitializer, DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.PageletInitializer
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.core.task.{FileManagerConsistencyTestTask, RenderTask, ResetPasswordRequestCleanerTask, TaskRunner}
import cz.kamenitxan.jakon.devtools.{DevRender, StaticFilesController, UploadFilesController}
import cz.kamenitxan.jakon.logging.{LogCleanerTask, Logger}
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailSendTask, EmailTemplateEntity}
import cz.kamenitxan.jakon.utils.{LoggingExceptionHandler, PageContext}
import cz.kamenitxan.jakon.webui.controller.impl.{DeployController, FileManagerController}
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, ResetPasswordEmailEntity}
import cz.kamenitxan.jakon.webui.{AdminSettings, Routes}
import spark.Spark.*
import spark.http.matching.Configuration
import spark.{Filter, Request, Response}

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit


class JakonInit {
	def daoSetup(): Unit = {
		// override to add custom DB entities
	}

	var websocketSetup: () => Unit = () => {}
	var routesSetup: () => Unit = () => {}

	def adminControllers(): Unit = {
		if (Files.exists(Paths.get("servers.json"))) {
			AdminSettings.registerCustomController(classOf[DeployController])
		}
		if (AdminSettings.enableFiles) {
			AdminSettings.registerCustomController(classOf[FileManagerController])
		}
	}

	def taskSetup(): Unit = {
		TaskRunner.registerTask(new RenderTask(10, TimeUnit.MINUTES))
		if (Settings.isEmailEnabled) {
			DBHelper.addDao(classOf[EmailEntity])
			DBHelper.addDao(classOf[EmailTemplateEntity])
			DBHelper.addDao(classOf[ConfirmEmailEntity])
			DBHelper.addDao(classOf[ResetPasswordEmailEntity])
			TaskRunner.registerTask(new EmailSendTask(1, TimeUnit.MINUTES))
			TaskRunner.registerTask(new ResetPasswordRequestCleanerTask)
		}
		TaskRunner.registerTask(new FileManagerConsistencyTestTask)
		TaskRunner.registerTask(new LogCleanerTask)
	}

	def afterInit(): Unit = {
		// override to do some stuff after Jakon start
	}

	def run(args: Array[String]): Unit = {
		val arguments = args.toList.map(a => {
			val split = a.split("=")
			split.length match {
				case 1 => split(0) -> ""
				case 2 => split(0) -> split(1)
			}
		})
		val configName = arguments.find(a => a._1 == "jakonConfig").map(a => a._2)
		val configFile = if (configName.nonEmpty) new File(configName.get) else null
		ConfigurationInitializer.init(configFile)
		val annotationScanner = new AnnotationScanner
		annotationScanner.loadConfiguration()

		Configuration.setDefaultcontentype("text/html; charset=utf-8")
		staticFiles.externalLocation(Settings.getStaticDir)
		staticFiles.location("/static")
		val portNumber: Int = arguments.find(a => a._1 == "port").map(a => a._2.toInt).getOrElse(Settings.getPort)
		port(portNumber)

		Logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()
		adminControllers()
		taskSetup()

		websocketSetup()
		before(new Filter {
			override def handle(req: Request, res: Response): Unit = PageContext.init(req, res)
		})
		afterAfter((_: Request, _: Response) => PageContext.destroy())
		if (Settings.getDeployMode != DeployMode.PRODUCTION) {
			before((request: Request, _: Response) => {
				DevRender.rerender(request.pathInfo())
			})
			exception(classOf[Exception], new LoggingExceptionHandler)

			get("/upload/*", (req: Request, res: Response) => new UploadFilesController().doGet(req, res))

			notFound((req: Request, res: Response) => new StaticFilesController().doGet(req, res))
		}
		routesSetup()
		annotationScanner.load()
		if (Settings.getDeployMode !=  DeployMode.DEVEL) {
			PageletInitializer.protectedPrefixes.filter(_ != Routes.AdminPrefix).foreach(pp => {
				before(pp + "*", new Filter {
					Logger.debug(s"Adding protected prefix '$pp*'")
					override def handle(req: Request, res: Response): Unit = {
						val user: JakonUser = req.session.attribute("user")
						if (user == null || (!user.acl.adminAllowed && !user.acl.allowedFrontendPrefixes.contains(pp))) {
							Logger.debug(s"User $user denied access to '$pp*'")
							if (req.pathInfo().startsWith(Routes.AdminPrefix)) {
								res.redirect(Routes.AdminPrefix + s"?redirectTo=${req.pathInfo()}", 302)
							} else {
								res.redirect(Settings.getLoginPath + s"?redirectTo=${req.pathInfo()}", 302)
							}
						}
					}
				} )
			})
		}
		Director.start()
		afterInit()
	}
}
