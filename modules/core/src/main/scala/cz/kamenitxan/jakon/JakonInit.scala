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
import cz.kamenitxan.jakon.webui.entity.{ConfirmEmailEntity, ResetPasswordEmailEntity}
import cz.kamenitxan.jakon.webui.{AdminSettings, Routes}
import io.github.classgraph.ScanResult
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.config.{JavalinConfig, RoutesConfig}
import io.javalin.http.{Context, Handler, HttpStatus}
import io.javalin.plugin.bundled.CorsPluginConfig

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit
import java.util.function.Consumer


class JakonInit {

	//noinspection ScalaWeakerAccess
	protected def javalinConfig(config: JavalinConfig): Unit = {
		// override to add custom Javalin config
	}

	protected def daoSetup(): Unit = {
		// override to add custom DB entities
	}

	//noinspection ScalaWeakerAccess
	protected def classScanHandler(scanResult: ScanResult): Unit = {
		// override to add class scan handler
	}

	//noinspection ScalaWeakerAccess
	protected def websocketSetup(config: JavalinConfig): Unit = {
		// override to add websocket setup
	}

	//noinspection ScalaWeakerAccess
	protected def routesSetup(routes: RoutesConfig): Unit = {
		// override to custom routes
	}

	//noinspection ScalaWeakerAccess
	protected def corsSetup(): Consumer[CorsPluginConfig] | Null = {
		// override to add custom CORS setup
		if (Settings.getDeployMode == DeployMode.DEVEL) {
			cors => {
				cors.addRule(it => {
					it.reflectClientOrigin = true
				})
			}
		} else {
			 null
		}
	}

	protected def adminControllers(): Unit = {
		if (Files.exists(Paths.get("servers.json"))) {
			// TODO skryvani controleru udelat nejak jinak
			//AdminSettings.registerCustomController(classOf[DeployController])
		}
		if (AdminSettings.enableFiles) {
			// TODO skryvani controleru udelat nejak jinak
			//AdminSettings.registerCustomController(classOf[FileManagerController])
		}
	}

	//noinspection ScalaWeakerAccess
	protected def taskSetup(): Unit = {
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

	//noinspection ScalaWeakerAccess
	protected def afterInit(): Unit = {
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
			config.http.defaultContentType = "text/html; charset=utf-8"
			config.router.treatMultipleSlashesAsSingleSlash = true
			config.staticFiles.add(Settings.getStaticDir)
			config.staticFiles.add("/static")
			config.registerPlugin(new ContextExtension)
			corsSetup() match {
				case null => // do nothing
				case corsConfig: Consumer[CorsPluginConfig] => config.bundledPlugins.enableCors(corsConfig)
			}
			javalinConfig(config)

			setupJavalin(config, annotationScanner)
		})
		
		app.start(portNumber)
		Logger.info(s"Jakon started on port $portNumber")
		Director.start()
		afterInit()
	}
	
	private def setupJavalin(config: JavalinConfig, annotationScanner: AnnotationScanner): Unit = {
		JakonInit.javalinConfig = config
		ApiBuilder.setStaticJavalin(config.routes)

		Logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()
		adminControllers()
		taskSetup()

		if (Settings.isInitRoutes) {
			websocketSetup(config)
			config.routes.before((ctx: Context) => {
				PageContext.init(ctx)
			})
			config.routes.after((ctx: Context) => PageContext.destroy())
			if (Settings.getDeployMode != DeployMode.PRODUCTION) {
				config.routes.before((ctx: Context) => {
					DevRender.rerender(ctx.path())
				})
				config.routes.exception(classOf[Exception], new LoggingExceptionHandler)

				config.routes.get("/upload/*", new Handler {
					override def handle(ctx: Context): Unit = {
						val res = new UploadFilesController().doGet(ctx)
						ctx.result(res)
					}
				})

				config.routes.error(404, new Handler {
					override def handle(ctx: Context): Unit = {
						new StaticFilesController().doGet(ctx)
					}
				})
			}
			routesSetup(config.routes)
			initDevMode(config)
		}


		annotationScanner.load(sr => classScanHandler(sr))
	}

	private def initDevMode(config: JavalinConfig): Unit = {
		if (Settings.getDeployMode != DeployMode.DEVEL) {
			PageletInitializer.protectedPrefixes.filter(_ != Routes.AdminPrefix).foreach(pp => {
				config.routes.before(pp + "*", (ctx: Context) => {
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

}

object JakonInit {
	var javalinConfig: JavalinConfig = _
}