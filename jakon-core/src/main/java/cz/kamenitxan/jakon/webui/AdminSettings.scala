package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.webui.controler.AbstractController
import cz.kamenitxan.jakon.webui.controler.impl.Dashboard
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable

object AdminSettings {
	var dashboardController: (Request, Response) => Context = (req: Request, res: Response) => Dashboard.getDashboard(req, res)
	var enableFiles = true
	val customControllers = new mutable.ListBuffer[Class[_ <: AbstractController]]
	val customControllersJava = {
		customControllers.asJava
	}
	val customControllersInfo = new mutable.ListBuffer[CustomControllerInfo]

	def registerCustomController[T <: AbstractController](controller: Class[T]): Unit = {
		val inst = controller.newInstance()
		customControllers += controller
		customControllersInfo += new CustomControllerInfo(inst.name(), inst.icon, "/admin/" + inst.path())
	}

	def setDashboardController(fun: (Request, Response) => Context): Unit = {
		dashboardController = fun
	}
}
