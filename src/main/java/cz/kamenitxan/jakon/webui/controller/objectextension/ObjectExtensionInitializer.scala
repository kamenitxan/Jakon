package cz.kamenitxan.jakon.webui.controller.objectextension


import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.AdminSettings


object ObjectExtensionInitializer {

	def initObjectExtensions(objectExtensions: Seq[Class[_]]): Unit = {
		Logger.info("Initializing ObjectExtensions")
		objectExtensions.foreach(oe => {
			Logger.debug("Initializing ObjectExtension: " + oe.getSimpleName)
			val ann = oe.getDeclaredAnnotation(classOf[ObjectExtension])
			AdminSettings.objectExtensions.addBinding(ann.value().asInstanceOf[Class[JakonObject]], oe.asInstanceOf[Class[AbstractObjectExtension]])
		})

		Logger.info("ObjectExtensions initialization complete")
	}


}
