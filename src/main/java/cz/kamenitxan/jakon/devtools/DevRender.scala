package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controller.IController
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils

import scala.collection.mutable

object DevRender {
	private val controllers: Seq[IController] = Director.controllers ::: Director.customPages
	private val registeredPaths = mutable.HashMap[String, IController]()

	def registerPath(path: String, caller: IController): Unit = {
		val controler = controllers.find(c => c.getClass.getCanonicalName != null && c.getClass.getCanonicalName.equals(caller.getClass.getCanonicalName))
		if (controler.isEmpty) return
		val prefix = if (path.startsWith("/")) {
			""
		} else {
			"/"
		}
		registeredPaths += (prefix + path + TemplateUtils.getFileSuffix(path) -> controler.get)
	}

	def rerender(path: String): Unit = {
		if (registeredPaths.contains(path)) {
			val controller: Option[IController] = registeredPaths.get(path)
			controller.get.generateRun()
		}
	}
}
