package cz.kamenitxan.jakon.core

import java.nio.charset.Charset

import cz.kamenitxan.jakon.JakonInitializer
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.custom_pages.AbstractCustomPage
import cz.kamenitxan.jakon.core.database.DBInitializer
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.Routes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	var customPages: List[IController] = List[IController]()
	var controllers: List[IController] = List[IController]()

	val SELECT_EMAIL_TMPL_SQL = "SELECT addressFrom, template, subject FROM EmailTemplateEntity WHERE name = ?"

	def init() {
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
		if (Settings.getDeployMode != DeployMode.PRODUCTION) {
			DBInitializer.createTables()
		} else {
			DBInitializer.dbExists()
		}
		Future {
			DBInitializer.checkDbConsistency()
		}

		TaskRunner.startTaskRunner()
		Routes.init()
		Logger.info("Jakon started")

		if (Settings.getDeployMode != DeployMode.PRODUCTION) {
			JakonInitializer.init()
		}

		Logger.info("Jakon default init complete")
	}

	def render() {
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
		//TODO: moznost vypnout administraci
		//TemplateUtils.copy("templates/admin/static", Settings.getOutputDir)
		Logger.info("Render complete")
	}

	def registerCustomPage(page: AbstractCustomPage) {
		customPages = customPages.::(page)

	}

	def registerControler(controler: IController) {
		controllers = controllers.::(controler)
	}
}