package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import org.slf4j.LoggerFactory
import spark.Spark.port
import spark.Spark.staticFiles
import java.io.IOException

open class JakonApp {
	private val logger = LoggerFactory.getLogger(this.javaClass)

	open fun daoSetup(): Unit {

	}

	open fun routesSetup(): Unit {

	}

	open fun run(): Unit {
		staticFiles.externalLocation(Settings.getOutputDir())
		port(Settings.getPort())

		try {
			Settings.init(null)
		} catch ( e: IOException) {
				logger.error("Jdufanit: cant load jakon settings", e)
			}
			logger.info("Starting in " + Settings.getDeployMode() + " mode")

			daoSetup()

			routesSetup()

			Director.start()

	}
}