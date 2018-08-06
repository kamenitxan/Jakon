package webui

import cz.kamenitxan.jakon.core.configuration.Settings
import org.openqa.selenium.firefox.FirefoxDriver
import org.scalatest.FunSuite

class MenuTest extends FunSuite  {


	test("menu items") {
		val host = "http://localhost:"  + (Settings.getPort -1)  + "/admin/"
		val driver = new FirefoxDriver()
		driver.get(host + "index")

		driver.close()
	}
}
