package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.customPages.{CustomPage, StaticPage}
import jdk.internal.reflect.Reflection

import scala.collection.mutable

object DevRender {
	private val controllers: Seq[IControler] = Director.controllers ::: Director.customPages
	private val registeredPaths = mutable.HashMap[String, IControler]()
	
	def registerPath(path: String): Unit = {
		Reflection.getCallerClass(1)
		val caller = {
			val stc = Thread.currentThread.getStackTrace(){3}.getClassName
			if (stc.equals("cz.kamenitxan.jakon.core.customPages.StaticPage")) {
				Director.customPages.find(cp => cp.asInstanceOf[StaticPage].url.equals(path))
			} else {
				stc
			}
		}
		val controler = controllers.find(c => c.getClass.getCanonicalName.equals(caller))
		if (controler.isEmpty) return
		registeredPaths += (path -> controler.get)
	}

	def rerender(path: String): Unit = {
		if (registeredPaths.contains(path)) {
			val controller: Option[IControler] = registeredPaths.get(path)
			controller.get.generate()
		}
	}
}
