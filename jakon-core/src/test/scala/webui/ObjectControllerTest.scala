package webui

import java.io.DataOutputStream
import java.net.{HttpURLConnection, URL}

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.{Outcome, fixture}

import scala.io.Source

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

	test("resetPassword") { f =>
		val url = "http://localhost:"  + (Settings.getPort) + "/admin/resetPassword"
		f.driver.get(url)
		//assert(checkPageLoad(f.driver))

		val emailInput = f.driver.findElement(By.cssSelector("input[type=email]"))
		emailInput.sendKeys("admin@admin.cz")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(f.driver.getPageSource.contains("Na váš email byl odeslán email pro změnu hesla"))
	}



	test("user settings") { f =>
		val url = "http://localhost:"  + (Settings.getPort) + "/admin/profile"
		f.driver.get(url)

		assert(checkPageLoad(f.driver))
		assert(f.driver.getPageSource.contains("admin"))
	}

}
