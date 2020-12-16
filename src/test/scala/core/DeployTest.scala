package core

import cz.kamenitxan.jakon.core.deploy.LocalDeploy
import cz.kamenitxan.jakon.core.deploy.entity.Server
import test.TestBase

class DeployTest extends TestBase {

	test("LocalDeployTest") { _ =>
		val s = new Server(3, "localhost", "localDeploy", null)

		val d = new LocalDeploy
		d.deploy(s)
	}

}
