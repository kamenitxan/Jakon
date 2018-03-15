package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.webui.controler.{CustomController, Dashboard}
import spark.{Request, Response}


object AdminSettings {
	var dashboardController = (req: Request, res: Response) => Dashboard.getDashboard(req ,res)
	var enableFiles = true
	var enableDeploy = true
	val customControllers = new java.util.ArrayList[CustomController]

	def registerCustomController(controller: CustomController): Unit = {
		customControllers.add(controller)
	}
}
