package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{Outcome, fixture}

class MenuTest extends fixture.FunSuite  {
	var host = ""


	case class FixtureParam(driver: WebDriver  )

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:"  + (Settings.getPort)  + "/admin/"
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		}
		finally driver
	}

	def checkPageLoad(driver: WebDriver) = {
		driver.findElements(By.cssSelector(".navbar-brand")).get(0) != null
	}

	test("Forgotten password items") { f =>
		f.driver.get(host + "resetPassword")
		assert(f.driver.findElements(By.cssSelector(".panel-title")).get(0) != null)
	}

	test("menu items") { f =>
		f.driver.get(host + "index")
		//val menuElements = f.driver.findElements(By.cssSelector("#side-menu li a"))
		assert(checkPageLoad(f.driver))
	}

	test("user page") { f =>
		f.driver.get(host + "object/JakonUser")
		assert(checkPageLoad(f.driver))
	}

	test("page page") { f =>
		f.driver.get(host + "object/Page")
		assert(checkPageLoad(f.driver))
	}

	test("deploy page") { f =>
		f.driver.get(host + "deploy")
		assert(checkPageLoad(f.driver))
	}

	test("task page") { f =>
		f.driver.get(host + "task")
		assert(checkPageLoad(f.driver))
	}

	test("files page") { f =>
		f.driver.get(host + "files/")
		assert(checkPageLoad(f.driver))
	}
}
