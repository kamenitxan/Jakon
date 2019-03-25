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
		val obj = new URL(url)
		val con = obj.openConnection.asInstanceOf[HttpURLConnection]


		con.setRequestMethod("POST")
		con.setDoOutput(true)
		val wr = new DataOutputStream(con.getOutputStream)
		wr.writeBytes("email=admin@admin.cz")
		wr.flush()

		val responseCode = con.getResponseCode
		val response= Source.fromInputStream(con.getInputStream).mkString

		//print result
		assert(responseCode == 200)
		assert(response.contains("Na váš email byl odeslán email pro změnu hesla"))
	}

	test("login") { f =>
		val url = "http://localhost:"  + (Settings.getPort) + "/admin"
		val obj = new URL(url)
		val con = obj.openConnection.asInstanceOf[HttpURLConnection]


		con.setRequestMethod("POST")
		con.setDoOutput(true)
		val wr = new DataOutputStream(con.getOutputStream)
		wr.writeBytes("email=admin@admin.cz&password=admin")
		wr.flush()

		val responseCode = con.getResponseCode
		val response= Source.fromInputStream(con.getInputStream).mkString

		//print result
		assert(responseCode == 200)
		assert(response.contains("Na váš email byl odeslán email pro změnu hesla"))
	}

}
