package cz.kamenitxan.jakon.core.deploy

import cz.kamenitxan.jakon.core.deploy.entity.Server

trait IDeploy {
	def deploy(server: Server): Unit

	def getDeploySettings: Map[String, String]
}
