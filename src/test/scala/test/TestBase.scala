package test

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.{Outcome, fixture}
import scala.collection.JavaConverters._


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

	protected def checkSiteMessage(msgText: String)(implicit driver: WebDriver) = {
		val msgs = driver.findElements(By.cssSelector("#jakon_messages .alert")).asScala.map(e => e.getText)
		assert(msgs.contains(msgText))
	}

	protected def findElements(selector: String)(implicit driver: WebDriver) = {
		driver.findElements(By.cssSelector(selector)).asScala
	}

	protected def getAdminTableRows()(implicit driver: WebDriver) = {
		findElements("#dataTables-example tbody tr")
	}

	protected def assertNotEmpty(v: String) = {
		assert(v != null)
		assert(v.nonEmpty)
	}
}
