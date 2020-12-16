package test

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.funsuite.FixtureAnyFunSuite
import org.scalatest.{Assertion, Outcome}

import scala.collection.mutable
import scala.jdk.CollectionConverters._


class TestBase extends FixtureAnyFunSuite {
	var host = ""
	var adminHost = "/admin/"

	case class FixtureParam(driver: WebDriver)

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:" + Settings.getPort
		adminHost = host + "/admin/"
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		} finally {
			driver.close()
		}

	}

	protected def checkPageLoad(selector: String = ".navbar-brand")(implicit driver: WebDriver): Boolean = {
		driver.findElements(By.cssSelector(selector)).get(0) != null
	}

	protected def checkSiteMessage(msgText: String)(implicit driver: WebDriver): Assertion = {
		val msgs = driver.findElements(By.cssSelector("#jakon_messages .alert")).asScala.map(e => e.getText)
		assert(msgs.contains(msgText))
	}

	protected def findElements(selector: String)(implicit driver: WebDriver): mutable.Buffer[WebElement] = {
		driver.findElements(By.cssSelector(selector)).asScala
	}

	protected def getAdminTableRows()(implicit driver: WebDriver): mutable.Buffer[WebElement] = {
		findElements("#dataTables-example tbody tr")
	}

	protected def assertNotEmpty(v: String): Assertion = {
		assert(v != null)
		assert(v.nonEmpty)
	}
}
