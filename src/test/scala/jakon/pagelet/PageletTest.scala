package jakon.pagelet

import cz.kamenitxan.jakon.core.dynamic.PageletInitializer
import test.TestBase

class PageletTest extends TestBase {

	test("example pagelet get") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val url = host + "/pagelet/get"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

	test("example pagelet post") { f =>
		PageletInitializer.initControllers(Seq(classOf[TestPagelet]))

		val postUrl = host + "/pagelet/post"
		f.driver.get(postUrl)

		val getUrl = host + "/pagelet/get"
		f.driver.get(getUrl)

		assert(f.driver.getPageSource.contains("pushedValue"))
	}

}
