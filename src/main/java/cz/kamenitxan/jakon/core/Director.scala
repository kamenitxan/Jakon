package cz.kamenitxan.jakon.core

import java.nio.charset.Charset

import cz.kamenitxan.jakon.JakonInitializer
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.customPages.AbstractCustomPage
import cz.kamenitxan.jakon.core.database.{DBHelper, DBInitializer}
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils
import cz.kamenitxan.jakon.webui.Routes
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	var customPages: List[IControler] = List[IControler]()
	var controllers: List[IControler] = List[IControler]()
	final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val SELECT_EMAIL_TMPL_SQL = "SELECT addressFrom, template FROM EmailTemplateEntity WHERE name = ?"

	def init() {
		Settings.setTemplateDir("templates/bacon/")
		Settings.setTemplateEngine(new Pebble)
		Settings.setOutputDir("out")
	}

	def start(): Unit = {
		val enc = Charset.defaultCharset()
		if (Charset.forName("UTF-8") != enc) {
			logger.warn(s"JVM character encoding $enc is not UTF-8")
		}

		if (Settings.getDeployMode.equals(DeployMode.DEVEL)) {
			DBInitializer.createTables()
		}
		Future {
			DBInitializer.checkDbConsistency()
		}

		TaskRunner.startTaskRunner()
		Routes.init()
		logger.info("Jakon started")

		if (Settings.getDeployMode.equals(DeployMode.DEVEL)) {
			JakonInitializer.init()
		}

		logger.info("Jakon default init complete")
	}

	def render() {
		TemplateUtils.clean(Settings.getOutputDir)
		controllers.foreach(i => {
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
		logger.info("Render complete")
	}

	def registerCustomPage(page: AbstractCustomPage) {
		customPages = customPages.::(page)

	}

	def registerControler(controler: IControler) {
		controllers = controllers.::(controler)
	}
}