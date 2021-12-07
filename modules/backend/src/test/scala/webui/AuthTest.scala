package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.controller.impl.Authentication
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.{DoNotDiscover, Outcome}
import org.scalatest.funsuite.FixtureAnyFunSuite

import java.util.Locale
import scala.util.Random

/**
  * Created by TPa on 03.09.16.
  */
@DoNotDiscover
class AuthTest extends FixtureAnyFunSuite {
	private val email = "test@gmail.com" + Random.nextInt()
	private val password = "paßßw0rd"
	private var host = ""


	case class FixtureParam(driver: WebDriver)

	def withFixture(test: OneArgTest): Outcome = {
		host = "http://localhost:" + Settings.getPort + "/admin/"
		val driver = new HtmlUnitDriver()

		val fixture = FixtureParam(driver)
		try {
			withFixture(test.toNoArgTest(fixture)) // "loan" the fixture to the test
		} finally {
			driver.close()
		}
	}

	private def checkPageLoad(driver: WebDriver, selector: String = ".navbar-brand") = {
		driver.findElements(By.cssSelector(selector)).get(0) != null
	}

	test("create user") { _ =>
		val user = new JakonUser()
		user.firstName = "testName"
		user.lastName = "lastName"
		user.email = email
		user.password = password
		user.locale = new Locale("en", "US")

		assert(user.create() > 0)
	}

	test("check password") { _ =>
		val sql = "SELECT id, username, password, enabled, acl_id FROM JakonUser WHERE email = ?"
		val user = DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement(sql)
			stmt.setString(1, email)
			val result = DBHelper.selectSingle(stmt, classOf[JakonUser])
			result.entity
		})

		assert(Authentication.checkPassword(password, user.password))
	}

	test("login ok") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin"
		f.driver.get(url)

		val emailInput = f.driver.findElement(By.cssSelector("input[type=email]"))
		emailInput.sendKeys("admin@admin.cz")
		val passwordInput = f.driver.findElement(By.cssSelector("input[type=password]"))
		passwordInput.sendKeys("admin")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test("login bad user") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin"
		f.driver.get(url)

		val emailInput = f.driver.findElement(By.cssSelector("input[type=email]"))
		emailInput.sendKeys("adminasdfasdfasdf@admin.cz")
		val passwordInput = f.driver.findElement(By.cssSelector("input[type=password]"))
		passwordInput.sendKeys("admin")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test("login wrong pass") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin"
		f.driver.get(url)

		val emailInput = f.driver.findElement(By.cssSelector("input[type=email]"))
		emailInput.sendKeys("admin@admin.cz")
		val passwordInput = f.driver.findElement(By.cssSelector("input[type=password]"))
		passwordInput.sendKeys("asdasdasdadsa")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test ("logout") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin/logout"
		f.driver.get(url)
		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test ("register get") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin/register"
		f.driver.get(url)
		assert(checkPageLoad(f.driver, ".card-header"))
	}

	test ("register post ok") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin/register"
		f.driver.get(url)

		val firstNameInput = f.driver.findElement(By.cssSelector("#firstname"))
		firstNameInput.sendKeys("unit")
		val lastNameInput = f.driver.findElement(By.cssSelector("#firstname"))
		lastNameInput.sendKeys("test")
		val emailInput = f.driver.findElement(By.cssSelector("#email"))
		emailInput.sendKeys("unittest@test.cz")
		val passwordInput1 = f.driver.findElement(By.cssSelector("#password"))
		passwordInput1.sendKeys("unittest")
		val passwordInput2 = f.driver.findElement(By.cssSelector("#password2"))
		passwordInput2.sendKeys("unittest")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test ("register post nok") { f =>
		val url = "http://localhost:"  + Settings.getPort + "/admin/register"
		f.driver.get(url)

		val firstNameInput = f.driver.findElement(By.cssSelector("#firstname"))
		firstNameInput.sendKeys("unit")
		val lastNameInput = f.driver.findElement(By.cssSelector("#firstname"))
		lastNameInput.sendKeys("test")
		val emailInput = f.driver.findElement(By.cssSelector("#email"))
		emailInput.sendKeys("unittest@test.cz")
		val passwordInput1 = f.driver.findElement(By.cssSelector("#password"))
		passwordInput1.sendKeys("unittest")
		val passwordInput2 = f.driver.findElement(By.cssSelector("#password2"))
		passwordInput2.sendKeys("asddasdas")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad(f.driver, "#jakon_messages"))
	}

	test("resetPasswordStep2 get") { f =>
		val url = "http://localhost:" + Settings.getPort + "/admin/resetPasswordStep2"
		f.driver.get(url)
	}
}
