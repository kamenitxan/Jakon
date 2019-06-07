package test

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.{Outcome, fixture}

class TestBase extends fixture.FunSuite{
	var host = ""
	val admin = "/admin/"

	case class FixtureParam(driver: WebDriver)

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:" + Settings.getPort
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		} finally {
			driver.close()
		}

	}

	protected def checkPageLoad(selector: String = ".navbar-brand")(implicit driver: WebDriver) = {
		driver.findElements(By.cssSelector(selector)).get(0) != null
	}
}
