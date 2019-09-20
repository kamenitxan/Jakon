package cz.kamenitxan.jakon.webui.controler.objectextension


import cz.kamenitxan.jakon.webui.AdminSettings
import org.slf4j.LoggerFactory


object ObjectExtensionInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def initObjectExtensions(objectExtensions: Seq[Class[_]]): Unit = {
		logger.info("Initializing ObjectExtensions")
		objectExtensions.foreach(oe => {
			logger.debug("Initializing ObjectExtension: " + oe.getSimpleName)
			AdminSettings.objectExtensions += oe.newInstance().asInstanceOf[AbstractObjectExtension]
		})

		logger.info("ObjectExtensions initialization complete")
	}


}
