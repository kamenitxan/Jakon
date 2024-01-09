package jakontest.webui

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.service.UserService
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.DoNotDiscover
import jakontest.test.{ReflectionUtils, TestBase}

/**
  * Created by TPa on 04.04.2020.
  */
@DoNotDiscover
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


	test("JakonUserLogAsExtension correct id") { f =>
		implicit val driver: WebDriver = f.driver
		DBHelper.withDbConnection(implicit conn => {
			val usr = UserService.getMasterAdmin()
			driver.get(host + s"/admin/object/JakonUser/${usr.id}/forceLogin")
			assert(checkPageLoad())
			checkSiteMessage("Logged as: admin")
		})
	}

	test("JakonUserExtension email disabled") { f =>
		implicit val driver: WebDriver = f.driver
		ReflectionUtils.changeObjectFieldValue("emailEnabled", "cz.kamenitxan.jakon.core.configuration.Settings", false)

		DBHelper.withDbConnection(implicit conn => {
			val usr = UserService.getMasterAdmin()
			driver.get(host + s"/admin/object/JakonUser/${usr.id}")
			assert(checkPageLoad())
		})
	}

	test("JakonUserEmailExtension email disabled") { f =>
		implicit val driver: WebDriver = f.driver

		driver.get(host + s"/admin/object/JakonUser")
		assert(checkPageLoad())
	}

	test("JakonUserExtension reset with email disabled") { f =>
		implicit val driver: WebDriver = f.driver

		DBHelper.withDbConnection(implicit conn => {
			val usr = UserService.getMasterAdmin()
			driver.get(host + s"/admin/object/JakonUser/${usr.id}/resetPassword")
			assert(checkPageLoad())
			checkSiteMessage("Reset password email failed")
		})
		ReflectionUtils.changeObjectFieldValue("emailEnabled", "cz.kamenitxan.jakon.core.configuration.Settings", true)
	}

	test("JakonUserExtension reset") { f =>
		implicit val driver: WebDriver = f.driver

		DBHelper.withDbConnection(implicit conn => {
			val usr = UserService.getMasterAdmin()
			driver.get(host + s"/admin/object/JakonUser/${usr.id}/resetPassword")
			assert(checkPageLoad())
			checkSiteMessage("Reset password email successfully sent")
		})
	}

}
