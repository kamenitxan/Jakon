package cz.kamenitxan.jakon

import java.io.IOException

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.core.{Director, Settings}
import org.slf4j.LoggerFactory
import spark.Spark.{port, staticFiles}

class JakonInit {
	private val logger = LoggerFactory.getLogger(this.getClass)

	var daoSetup = () => {
		//DBHelper.addDao(classOf[Post])
		//DBHelper.addDao(classOf[Page])
		//DBHelper.addDao(classOf[Category])
	}

	var routesSetup = () => {}

	def run(): Unit = {
		staticFiles.externalLocation(Settings.getOutputDir)
		port(Settings.getPort)

		try
			Settings.init(null)
		catch {
			case e: IOException => logger.error("Jdufanit: cant load jakon settings", e)
		}
		logger.info("Starting in " + Settings.getDeployMode + " mode")

		daoSetup()

		Director.start()

		routesSetup()
	}
}
