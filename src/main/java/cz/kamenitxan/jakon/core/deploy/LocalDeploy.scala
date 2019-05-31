package cz.kamenitxan.jakon.core.deploy

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.deploy.entity.Server
import cz.kamenitxan.jakon.core.template.TemplateUtils

class LocalDeploy extends IDeploy {
	override def deploy(server: Server): Unit = {
		TemplateUtils.clean(server.path)
		TemplateUtils.copy(Settings.getOutputDir, server.path)
	}

	override def getDeploySettings = ???
}
