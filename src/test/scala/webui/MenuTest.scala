package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.scalatest.{Outcome, fixture}

class MenuTest extends fixture.FunSuite  {
	val host = "http://localhost:"  + (Settings.getPort -1)  + "/admin/"


	case class FixtureParam(driver: FirefoxDriver)

	def withFixture(test: OneArgTest): Outcome = {
		val opt = new FirefoxOptions
		opt.addArguments("-headless")
		val driver = new FirefoxDriver(opt)

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		}
		finally driver.close()
	}


	test("menu items") { f =>
		f.driver.get(host + "index")
	}
}
