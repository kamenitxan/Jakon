package cz.kamenitxan.jakon.core.deploy

import cz.kamenitxan.jakon.core.deploy.entity.Server

class DummyDeploy extends IDeploy {
	override def deploy(server: Server): Unit = return

	override def getDeploySettings: Map[String, String] = Map[String, String]()
}
