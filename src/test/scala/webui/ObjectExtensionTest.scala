package webui

import java.util.Locale

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.UserService
import org.openqa.selenium.{By, WebDriver}
import test.TestBase

/**
 * Created by TPa on 04.04.2020.
 */
class ObjectExtensionTest extends TestBase {
	val templateDir = "templates/admin/"
	val dictionary = "messages"

	test("JakonUserEmailExtension list type not selected") { f =>
		implicit val driver: WebDriver = f.driver
		driver.get(host + "/admin/object/JakonUser")
		assert(checkPageLoad())

		val btn = driver.findElement(By.id("JakonUserEmailExtensionBtn"))
		btn.click()

		checkSiteMessage("Choose email type")
	}

	test("JakonUserEmailExtension single type not selected") { f =>
		implicit val driver: WebDriver = f.driver
		DBHelper.withDbConnection(implicit conn => {
		val usr = UserService.getMasterAdmin()

		driver.get(host + s"/admin/object/JakonUser/${usr.id}")
		assert(checkPageLoad())

		val btn = driver.findElement(By.id("JakonUserEmailExtensionBtn"))
		btn.click()

		checkSiteMessage("Choose email type")
	})

	}

}
