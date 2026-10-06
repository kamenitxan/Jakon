package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.configuration.ConfigurationInitializer
import cz.kamenitxan.jakon.core.custom_pages.CustomPageInitializer
import cz.kamenitxan.jakon.core.dynamic.PageletInitializer
import cz.kamenitxan.jakon.webui.AdminSettings
import jakontest.test.TestBase
import org.openqa.selenium.By
import org.scalatest.DoNotDiscover

@DoNotDiscover
class PageletTest extends TestBase {

	test("example pagelet get") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val url = host + "/pagelet/get"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

	test("example pagelet post") { f =>

		val postUrl = host + "/pagelet/get"
		f.driver.get(postUrl)

		val submit = f.driver.findElement(By.cssSelector("#testSubmit"))
		submit.click()

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

	test("example pagelet post - string") { f =>

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
			case ex: Throwable =>
				ex.printStackTrace()
				fail("Exception not excepted")
		}
	}

	test("CustomPageInitializer initStaticPages wrong class") { _ =>
		try {
			val cls = Seq(classOf[Object], classOf[Integer])
			CustomPageInitializer.initStaticPages(cls)
		} catch {
			case ex: Throwable =>
				ex.printStackTrace()
				fail("Exception not excepted")
		}
	}

	test("HealthCheckPagelet get") { f =>
		val url = host + "/jakon/health"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("JAKON_OK"))
	}

	test("disabled pagelet does not register its routes") { f =>
		val key = "pagelet." + classOf[TestDisabledPagelet].getSimpleName
		ConfigurationInitializer.getConf.put(key, "DISABLED")
		try {
			PageletInitializer.initControllers(Seq(classOf[TestDisabledPagelet]))

			f.driver.get(host + "/disabledPagelet/get")
			assert(!f.driver.getPageSource.contains("disabledPageletValue"))
		} finally {
			ConfigurationInitializer.getConf.remove(key)
		}
	}

	test("disabled admin pagelet is not shown in admin menu") { _ =>
		val key = "pagelet." + classOf[TestDisabledAdminPagelet].getSimpleName
		ConfigurationInitializer.getConf.put(key, "DISABLED")
		try {
			PageletInitializer.initControllers(Seq(classOf[TestDisabledAdminPagelet]))

			assert(!AdminSettings.customControllersInfo.exists(_.cls == classOf[TestDisabledAdminPagelet]))
		} finally {
			ConfigurationInitializer.getConf.remove(key)
		}
	}

	test("enabled admin pagelet is shown in admin menu") { _ =>
		try {
			PageletInitializer.initControllers(Seq(classOf[TestVisibleAdminPagelet]))

			assert(AdminSettings.customControllersInfo.exists(_.cls == classOf[TestVisibleAdminPagelet]))
		} finally {
			AdminSettings.customControllersInfo.filterInPlace(_.cls != classOf[TestVisibleAdminPagelet])
		}
	}

	test("disabled pagelet does not register protected prefix") { _ =>
		val key = "pagelet." + classOf[TestAuthRequiredPagelet].getSimpleName
		ConfigurationInitializer.getConf.put(key, "DISABLED")
		try {
			PageletInitializer.initControllers(Seq(classOf[TestAuthRequiredPagelet]))

			assert(!PageletInitializer.protectedPrefixes.contains("/authRequiredPagelet"))
		} finally {
			ConfigurationInitializer.getConf.remove(key)
		}
	}

}
