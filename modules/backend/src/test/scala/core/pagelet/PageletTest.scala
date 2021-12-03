package core.pagelet

import cz.kamenitxan.jakon.core.custom_pages.CustomPageInitializer
import cz.kamenitxan.jakon.core.dynamic.PageletInitializer
import org.openqa.selenium.By
import org.scalatest.DoNotDiscover
import test.TestBase

@DoNotDiscover
class PageletTest extends TestBase {

	test("example pagelet get") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val url = host + "/pagelet/get"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

	test("example pagelet post") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val postUrl = host + "/pagelet/get"
		f.driver.get(postUrl)

		val submit = f.driver.findElement(By.cssSelector("#testSubmit"))
		submit.click()

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

	test("example pagelet post - string") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val postUrl = host + "/pagelet/get"
		f.driver.get(postUrl)

		val submit = f.driver.findElement(By.cssSelector("#testSubmit2"))
		submit.click()

		assert(f.driver.getPageSource.contains("StringResponse"))
	}


	test("CustomPageInitializer initCustomPages wrong class") { _ =>
		try {
			val cls = Seq(classOf[Object], classOf[Integer])
			CustomPageInitializer.initCustomPages(cls)
		} catch {
			case _: Throwable => fail("Exception not excepted")
		}
	}

	test("CustomPageInitializer initStaticPages wrong class") { _ =>
		try {
			val cls = Seq(classOf[Object], classOf[Integer])
			CustomPageInitializer.initStaticPages(cls)
		} catch {
			case _: Throwable => fail("Exception not excepted")
		}
	}

	test("HealthCheckPagelet get") { f =>
		val url = host + "/jakon/health"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("JAKON_OK"))
	}


}
