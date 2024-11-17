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
import cz.kamenitxan.jakon.utils.{ContextExtension, LoggingExceptionHandler, PageContext}
import cz.kamenitxan.jakon.webui.controller.impl.{DeployController, FileManagerController}
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, ResetPasswordEmailEntity}
import cz.kamenitxan.jakon.webui.{AdminSettings, Routes}
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.http.{Context, Handler, HttpStatus}

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

		val portNumber: Int = arguments.find(a => a._1 == "port").map(a => a._2.toInt).getOrElse(Settings.getPort)

		val app = Javalin.create(config => {
			config.jetty.defaultPort = portNumber
			// config.useVirtualThreads = true TODO: config
			config.http.defaultContentType = "text/html; charset=utf-8"
			config.staticFiles.add(Settings.getStaticDir)
			config.staticFiles.add("/static")
			config.registerPlugin(new ContextExtension)
		})
		JakonInit.javalin = app
		ApiBuilder.setStaticJavalin(app)

		Logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()
		adminControllers()
		taskSetup()

		if (Settings.isInitRoutes) {
			websocketSetup()
			app.before((ctx: Context) => {
				PageContext.init(ctx)
			})
			app.after((ctx: Context) => PageContext.destroy())
			if (Settings.getDeployMode != DeployMode.PRODUCTION) {
				app.before((ctx: Context) => {
					DevRender.rerender(ctx.path())
				})
				app.exception(classOf[Exception], new LoggingExceptionHandler)

				app.get("/upload/*", new Handler {
					override def handle(ctx: Context): Unit = {
						val res = new UploadFilesController().doGet(ctx)
						ctx.result(res)
					}
				})

				app.error(404, new Handler { // TODO
					override def handle(ctx: Context): Unit = {
						val res = new StaticFilesController().doGet(ctx)
						//ctx.result(res)
					}
				})
			}
			routesSetup()
			if (Settings.getDeployMode != DeployMode.DEVEL) {
				PageletInitializer.protectedPrefixes.filter(_ != Routes.AdminPrefix).foreach(pp => {
					app.before(pp + "*", (ctx: Context) => {
						Logger.debug(s"Adding protected prefix '$pp*'")

						val user: JakonUser = ctx.sessionAttribute("user")
						if (user == null || (!user.acl.adminAllowed && !user.acl.allowedFrontendPrefixes.contains(pp))) {
							Logger.debug(s"User $user denied access to '$pp*'")
							if (ctx.path().startsWith(Routes.AdminPrefix)) {
								ctx.redirect(Routes.AdminPrefix + s"?redirectTo=${ctx.path()}", HttpStatus.FOUND)
							} else {
								ctx.redirect(Settings.getLoginPath + s"?redirectTo=${ctx.path()}", HttpStatus.FOUND)
							}
						}

					})
				})
			}
		}


		annotationScanner.load()
		app.start(Settings.getPort)
		Director.start()
		afterInit()
	}
}
object JakonInit {
	var javalin: Javalin = _
}