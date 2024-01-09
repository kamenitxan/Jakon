package jakontest.core

import cz.kamenitxan.jakon.core.deploy.LocalDeploy
import cz.kamenitxan.jakon.core.deploy.entity.Server
import org.scalatest.DoNotDiscover
import jakontest.test.TestBase

@DoNotDiscover
class DeployTest extends TestBase {

	test("LocalDeployTest") { _ =>
		val s = new Server(3, "localhost", "localDeploy", null)

		val d = new LocalDeploy
		d.deploy(s)
	}

}
