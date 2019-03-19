package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{Outcome, fixture}

/**
  * Created by TPa on 2019-03-19.
  */
class ObjectControllerTest extends fixture.FunSuite {
	var host = ""


	case class FixtureParam(driver: WebDriver)

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:" + (Settings.getPort) + "/admin/"
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		}
		finally driver
	}

	private def checkPageLoad(driver: WebDriver) = {
		driver.findElements(By.cssSelector(".navbar-brand")).get(0) != null
	}


}
