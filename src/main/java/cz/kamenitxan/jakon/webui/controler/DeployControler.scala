package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.core.configuration.{SettingValue, Settings}
import cz.kamenitxan.jakon.core.deploy.DeployDirector
import cz.kamenitxan.jakon.webui.Context
import spark.{ModelAndView, Request, Response}

object DeployControler {


	def getOverview(req: Request, res: Response): ModelAndView = {

			new Context(Map[String, Any](
				"deployMode" -> Settings.getDeployMode,
				"deployType" -> Settings.getProperty(SettingValue.DEPLOY_TYPE),
				"servers" -> DeployDirector.servers
			), "pages/deploy")
	}
}
