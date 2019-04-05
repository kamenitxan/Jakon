package cz.kamenitxan.jakon.core.customPages

import cz.kamenitxan.jakon.core.Director
import org.slf4j.LoggerFactory

object CustomPageInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass)

	def initCustomPages(customPages: Seq[Class[_]]): Unit = {
		logger.info("Initializing custom pages")
		customPages.foreach(cp => {
			logger.info("Initializing custom page: " + cp.getSimpleName)
			if (!isChildOf(cp, classOf[AbstractCustomPage])) {
				logger.error(cp.getSimpleName + " is not child of AbstractCustomPage")
				return
			}
			Director.registerCustomPage(cp.newInstance().asInstanceOf[AbstractCustomPage])
		})
		logger.info("Initializing custom pages complete")
	}

	def initStaticPages(customPages: Seq[Class[_]]): Unit = {
		logger.info("Initializing static pages")
		customPages.foreach(cp => {
			logger.info("Initializing static page: " + cp.getSimpleName)
			if (!isChildOf(cp, classOf[AbstractStaticPage])) {
				logger.error(cp.getSimpleName + " is not child of AbstractStaticPage")
				return
			}
			Director.registerCustomPage(cp.newInstance().asInstanceOf[AbstractStaticPage])
		})
		logger.info("Initializing static pages complete")
	}

	private def isChildOf(child: Class[_], parent: Class[_]):Boolean = {
		val supperClass = child.getSuperclass
		if (supperClass == null) {
			return false
		}
		if (supperClass.isAssignableFrom(parent)) {
			true
		} else {
			isChildOf(supperClass, parent)
		}
	}
}
