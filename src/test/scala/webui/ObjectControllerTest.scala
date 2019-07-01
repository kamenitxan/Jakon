package webui

import cz.kamenitxan.jakon.core.model.Page
import org.openqa.selenium.{By, WebDriver}
import test.TestBase

import scala.collection.JavaConverters._

/**
  * Created by TPa on 2019-03-19.
  */
class ObjectControllerTest extends TestBase {

	private def checkPageLoad(driver: WebDriver) = {
		driver.findElements(By.cssSelector(".navbar-brand")).get(0) != null
	}

	test("resetPassword") { f =>
		val url = host + "/admin/resetPassword"
		f.driver.get(url)
		//assert(checkPageLoad(f.driver))

		val emailInput = f.driver.findElement(By.cssSelector("input[type=email]"))
		emailInput.sendKeys("admin@admin.cz")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(f.driver.getPageSource.contains("Na váš email byl odeslán email pro změnu hesla"))


	}



	test("user settings") { f =>
		val url = host + "/admin/profile"
		f.driver.get(url)

		assert(checkPageLoad(f.driver))
		assert(f.driver.getPageSource.contains("admin"))

		val submit = f.driver.findElement(By.cssSelector(".btn.btn-primary"))
		submit.click()

		assert(checkPageLoad(f.driver))
		assert(f.driver.getPageSource.contains("admin"))
	}

	test("test list filter") { f =>
		val url = host + "/admin/object/JakonUser?filter_id=2&filter_published=true&filter_enabled=&filter_lastName=Admin&filter_firstName=Adm*&filter_emailConfirmed=&filter_email=&filter_username="
		f.driver.get(url)

		assert(checkPageLoad(f.driver))
	}

	test("test move") { f =>
	    implicit val driver = f.driver
	    val p1 = new Page()
		p1.title = "page1"
		p1.create()
		val p2 = new Page()
		p2.title = "page2"
		p2.create()
		val p3 = new Page()
		p3.title = "page3"
	    p3.create()

		val url = host + "/admin/object/Page"
		f.driver.get(url)

		assert(checkPageLoad(f.driver))
		val objects = getAdminTableRows()
		assert(objects.nonEmpty)

		val first = objects.head
		val firstElements = first.findElements(By.cssSelector("td")).asScala
		val firstId = firstElements.head.getText


		f.driver.get(host + s"/admin/object/moveDown/Page/$firstId?currentOrder=1")
		f.driver.get(url)
		assert(checkPageLoad(f.driver))


		val objects2 = findElements("#dataTables-example tbody tr")
		assert(objects2.nonEmpty)

		val second = objects2.tail.head
		val secondElements = second.findElements(By.cssSelector("td")).asScala
		val secondId = secondElements.head.getText
		val secondOrder = secondElements.tail.head.getText

		assert(firstId == secondId)
		assert("2" == secondOrder)
	}

	test("test move not ordered") { f =>
		implicit val driver = f.driver
		f.driver.get(host + "/admin/object/moveDown/JakonUser/4?currentOrder=1")
		checkSiteMessage("OBJECT_NOT_ORDERED")
	}

	test("delete item") { f =>
		implicit val driver = f.driver
		val url = host + "/admin/object/Page"
		f.driver.get(url)
		assert(checkPageLoad(f.driver))

		val objects = getAdminTableRows()
		assert(objects.nonEmpty)
		val first = objects.head
		val firstElements = first.findElements(By.cssSelector("td")).asScala
		val firstId = firstElements.head.getText


		f.driver.get(host + s"/admin/object/delete/Page/$firstId")
		f.driver.get(url)
		assert(checkPageLoad(f.driver))

		val objects2 = findElements("#dataTables-example tbody tr")
		assert(objects2.nonEmpty)

		val second = objects2.head
		val secondElements = second.findElements(By.cssSelector("td")).asScala
		val secondId = secondElements.head.getText

		assert(firstId != secondId)
	}

}
