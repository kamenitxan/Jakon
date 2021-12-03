package cz.kamenitxan.jakon.core.custom_pages

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.logging.Logger

object CustomPageInitializer {

	def initCustomPages(customPages: Seq[Class[_]]): Unit = {
		Logger.info("Initializing custom pages")
		customPages.foreach(cp => {
			Logger.info("Initializing custom page: " + cp.getSimpleName)
			if (!isChildOf(cp, classOf[AbstractCustomPage])) {
				Logger.error(cp.getSimpleName + " is not child of AbstractCustomPage")
				return
			}
			Director.registerCustomPage(cp.newInstance().asInstanceOf[AbstractCustomPage])
		})
		Logger.info("Initializing custom pages complete")
	}

	def initStaticPages(customPages: Seq[Class[_]]): Unit = {
		Logger.info("Initializing static pages")
		customPages.foreach(cp => {
			Logger.info("Initializing static page: " + cp.getSimpleName)
			if (!isChildOf(cp, classOf[AbstractStaticPage])) {
				Logger.error(cp.getSimpleName + " is not child of AbstractStaticPage")
				return
			}
			Director.registerCustomPage(cp.newInstance().asInstanceOf[AbstractStaticPage])
		})
		Logger.info("Initializing static pages complete")
	}

	@scala.annotation.tailrec
	private def isChildOf(child: Class[_], parent: Class[_]): Boolean = {
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
