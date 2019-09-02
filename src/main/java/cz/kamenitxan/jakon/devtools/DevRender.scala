package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.template.utils.TemplateUtils

import scala.collection.mutable

object DevRender {
	private val controllers: Seq[IControler] = Director.controllers ::: Director.customPages
	private val registeredPaths = mutable.HashMap[String, IControler]()

	def registerPath(path: String, caller: IControler): Unit = {
		val controler = controllers.find(c => c.getClass.getCanonicalName != null && c.getClass.getCanonicalName.equals(caller.getClass.getCanonicalName))
		if (controler.isEmpty) return
		registeredPaths += ("/" + path + TemplateUtils.getFileSuffix(path) -> controler.get)
	}

	def rerender(path: String): Unit = {
		if (registeredPaths.contains(path)) {
			val controller: Option[IControler] = registeredPaths.get(path)
			controller.get.generateRun()
		}
	}
}
