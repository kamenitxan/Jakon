package cz.kamenitxan.jakon.webui.controler.impl

import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.deploy.DeployDirector
import cz.kamenitxan.jakon.webui.Context
import spark.{ModelAndView, Request, Response}

object DeployControler {


	def getOverview(req: Request, res: Response): ModelAndView = {


			new Context(Map[String, Any](
				"deployMode" -> Settings.getDeployMode,
				"deployType" -> Settings.getProperty(SettingValue.DEPLOY_TYPE),
				"servers" -> DeployDirector.servers.asJava
			), "pages/deploy")
	}

	def deploy(req: Request, res: Response): ModelAndView = {
		DeployDirector.deploy()
		res.redirect("/admin/deploy/")
		new Context(Map[String, Any](), "pages/deploy")
	}
}
