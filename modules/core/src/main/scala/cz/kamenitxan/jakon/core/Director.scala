package cz.kamenitxan.jakon.core

import cz.kamenitxan.jakon.JakonInitializer
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.custom_pages.AbstractCustomPage
import cz.kamenitxan.jakon.core.database.DBInitializer
import cz.kamenitxan.jakon.core.task.{RenderTask, TaskRunner}
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.logging.{LogService, Logger}
import cz.kamenitxan.jakon.webui.Routes

import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	var customPages: List[IController] = List[IController]()
	var controllers: List[IController] = List[IController]()

	def init(): Unit = {
		Settings.setTemplateDir("templates/bacon/")
		Settings.setTemplateEngine(new Pebble)
		Settings.setOutputDir("out")
	}

	def start(): Unit = {
		val enc = Charset.defaultCharset()
		if (Charset.forName("UTF-8") != enc) {
			Logger.warn(s"JVM character encoding $enc is not UTF-8")
		}

		DBInitializer.registerCoreObjects()
		DBInitializer.createTables()
		DBInitializer.dbExists()
		Future {
			DBInitializer.checkDbConsistency()
		}

		if (Settings.isOnlyRender) {
			TaskRunner.runSingle(new RenderTask(5, TimeUnit.MINUTES))
			if (TaskRunner.shutdown()) {
				if (LogService.criticalCount > 0) {
					System.exit(2)
				} else if (LogService.errorCount > 0) {
					System.exit(3)
				} else {
					System.exit(0)
				}
			} else {
				System.exit(1)
			}
		} else {
			TaskRunner.startTaskRunner()
		}
		Routes.init()
		Logger.info("Jakon started")

		JakonInitializer.init()

		Logger.info("Jakon default init complete")
	}

	def render(): Unit = {
		TemplateUtils.clean(Settings.getOutputDir)
		controllers.foreach(i => {
			//TODO: poslat chybu dale ale neukoncit generovani
			i.generateRun()
		})
		customPages.foreach(i => {
			i.generateRun()
		})

		if (Settings.getStaticDir != null && Settings.getOutputDir != null) {
			TemplateUtils.copy(Settings.getStaticDir, Settings.getOutputDir)
		}
		Logger.info("Render complete")
	}

	def registerCustomPage(page: AbstractCustomPage): Unit = {
		customPages = customPages.::(page)

	}

	def registerController(controller: IController): Unit = {
		controllers = controllers.::(controller)
	}
}