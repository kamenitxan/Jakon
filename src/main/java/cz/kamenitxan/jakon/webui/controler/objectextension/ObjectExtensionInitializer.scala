package cz.kamenitxan.jakon.webui.controler.objectextension


import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.AdminSettings
import org.slf4j.LoggerFactory


object ObjectExtensionInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def initObjectExtensions(objectExtensions: Seq[Class[_]]): Unit = {
		logger.info("Initializing ObjectExtensions")
		objectExtensions.foreach(oe => {
			logger.debug("Initializing ObjectExtension: " + oe.getSimpleName)
			val ann = oe.getDeclaredAnnotation(classOf[ObjectExtension])
			AdminSettings.objectExtensions.addBinding(ann.value().asInstanceOf[Class[JakonObject]], oe.asInstanceOf[Class[AbstractObjectExtension]])
		})

		logger.info("ObjectExtensions initialization complete")
	}


}
