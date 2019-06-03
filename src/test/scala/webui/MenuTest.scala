package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.{Outcome, fixture}

import scala.util.control.Breaks._

class MenuTest extends fixture.FunSuite {
	var host = ""


	case class FixtureParam(driver: WebDriver)

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:" + (Settings.getPort) + "/admin/"
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		} finally {
			driver.close()
		}
	}

	private def checkPageLoad(driver: WebDriver) = {
		val navbar = driver.findElements(By.cssSelector(".navbar-brand"))
		if (navbar.isEmpty) {
			println(driver.getPageSource)
			false
		} else {
			navbar.get(0) != null
		}
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
		f.driver.get(host + "object/JakonUser/6")

		assert(checkPageLoad(f.driver))
		val submit = f.driver.findElement(By.cssSelector("input.btn.btn-primary"))
		submit.click()
	}

	test("page page") { f =>
		f.driver.get(host + "object/Page")
		assert(checkPageLoad(f.driver))
	}

	test("deploy page") { f =>
		f.driver.get(host + "deploy")
		assert(checkPageLoad(f.driver))
	}

	test("deploy page - start") { f =>
		f.driver.get(host + "deploy/start")
		assert(checkPageLoad(f.driver))
	}

	test("deploy page - generate") { f =>
		f.driver.get(host + "deploy/generate")
		assert(checkPageLoad(f.driver))
	}

	test("task page") { f =>
		f.driver.get(host + "task")
		assert(checkPageLoad(f.driver))
	}

	test("task page - run") { f =>
		f.driver.get(host + "task/run/RenderTask")
		assert(checkPageLoad(f.driver))
	}

	test("files page") { f =>
		f.driver.get(host + "files/")
		assert(checkPageLoad(f.driver))
	}
}
