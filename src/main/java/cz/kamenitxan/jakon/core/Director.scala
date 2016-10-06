package cz.kamenitxan.jakon.core

import com.mongodb.MongoClient
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.customPages.CustomPage
import cz.kamenitxan.jakon.core.model.Dao.MongoHelper
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.template.TemplateUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	private var customPages = List[CustomPage]()
	private var controlers = List[IControler]()
	final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	def init() {
		Settings.setTemplateDir("templates/bacon/")
		Settings.setTemplateEngine(new Pebble)
		Settings.setOutputDir("out")
		Settings.setDatabaseDriver("org.sqlite.JDBC")
		Settings.setDatabaseConnPath("jdbc:sqlite:jakon.sqlite")
		//MongoHelper.setDbName("jakon")
	}

	def render() {
		TemplateUtils.clean(Settings.getOutputDir)
		controlers.foreach(i => {
			val startTime = System.currentTimeMillis()
			i.generate()
			val stopTime = System.currentTimeMillis()
			val elapsedTime = stopTime - startTime
			logger.info(i.getClass.getSimpleName + " generated in " + elapsedTime + " ms")
		})
		customPages.foreach(i => {
			val startTime = System.currentTimeMillis()
			i.render()
			val stopTime = System.currentTimeMillis()
			val elapsedTime = stopTime - startTime
			logger.info(i.getClass.getSimpleName + " generated in " + elapsedTime + " ms")
		})

		if (Settings.getStaticDir != null && Settings.getOutputDir != null) {
			TemplateUtils.copy(Settings.getStaticDir, Settings.getOutputDir)
		}
		//TODO: moznost vypnout administraci
		TemplateUtils.copy("templates/admin/static", Settings.getOutputDir)
		logger.info("Render complete")
	}

	def registerCustomPage(page: CustomPage) {
		customPages = customPages.::(page)

	}

	def registerControler(controler: IControler) {
		controlers = controlers.::(controler)
	}
}