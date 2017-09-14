package cz.kamenitxan.jakon.core.deploy

class DummyDeploy extends IDeploy {
	override def deploy(server: Server): Unit = return

	override def getDeploySettings: Map[String, String] = Map[String, String]()
}
