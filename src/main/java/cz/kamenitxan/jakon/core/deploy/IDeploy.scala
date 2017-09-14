package cz.kamenitxan.jakon.core.deploy

trait IDeploy {
	def deploy(server: Server): Unit

	def getDeploySettings: Map[String, String]
}
