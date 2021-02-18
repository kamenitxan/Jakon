package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.controller.AbstractController
import cz.kamenitxan.jakon.webui.controller.impl.Dashboard
import cz.kamenitxan.jakon.webui.controller.objectextension.AbstractObjectExtension
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import spark.{Request, Response}

import java.util
import scala.jdk.CollectionConverters._
import scala.collection.mutable

object AdminSettings {
	var dashboardController: (Request, Response) => Context = (req: Request, res: Response) => Dashboard.getDashboard(req, res)
	var enableFiles = true
	val customControllers = new mutable.ListBuffer[Class[_ <: AbstractController]]
	val customControllersJava: util.List[Class[_ <: AbstractController]] = {
		customControllers.asJava
	}
	val customControllersInfo = new mutable.ListBuffer[CustomControllerInfo]
	val objectExtensions = new mutable.HashMap[Class[_ <: JakonObject], mutable.Set[Class[_ <: AbstractObjectExtension]]]() with mutable.MultiMap[Class[_ <: JakonObject], Class[_ <: AbstractObjectExtension]]

	def registerCustomController[T <: AbstractController](controller: Class[T]): Unit = {
		val inst = controller.newInstance()
		customControllers += controller
		customControllersInfo += new CustomControllerInfo(inst.name(), inst.icon, "/admin/" + inst.path(), controller)
	}

	def setDashboardController(fun: (Request, Response) => Context): Unit = {
		dashboardController = fun
	}
}
