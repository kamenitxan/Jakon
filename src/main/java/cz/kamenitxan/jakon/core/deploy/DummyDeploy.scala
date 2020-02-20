package cz.kamenitxan.jakon.core.deploy

import cz.kamenitxan.jakon.core.deploy.entity.Server

class DummyDeploy extends IDeploy {
	override def deploy(server: Server): Unit = {
		// Dummy deploy does nothing
	}
}
