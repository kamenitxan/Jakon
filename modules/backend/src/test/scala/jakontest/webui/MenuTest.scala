package jakontest.webui

import jakontest.test.TestBase
import org.openqa.selenium.By
import org.scalatest.DoNotDiscover

@DoNotDiscover
class MenuTest extends TestBase {


	test("Forgotten password items") { f =>
		f.driver.get(adminHost + "resetPassword")
		assert(f.driver.findElements(By.cssSelector(".card-title")).get(0) != null)
	}

	test("menu items") { f =>
		f.driver.get(adminHost + "index")
		//val menuElements = f.driver.findElements(By.cssSelector("#side-menu li a"))
		assert(checkPageLoad()(f.driver))
	}

	test("user page") { f =>
		f.driver.get(adminHost + "object/JakonUser")
		assert(checkPageLoad()(f.driver))
		f.driver.get(adminHost + "object/JakonUser/6")

		assert(checkPageLoad()(f.driver))
		val submit = f.driver.findElement(By.cssSelector("input.btn.btn-primary"))
		submit.click()
	}

	test("acl page") { f =>
		f.driver.get(adminHost + "object/AclRule")
		assert(checkPageLoad()(f.driver))
		f.driver.get(adminHost + "object/create/AclRule")

		assert(checkPageLoad()(f.driver))
	}

	test("page page") { f =>
		f.driver.get(adminHost + "object/Page")
		assert(checkPageLoad()(f.driver))
	}

	test("deploy page") { f =>
		f.driver.get(adminHost + "deploy")
		assert(checkPageLoad()(f.driver))
	}

	test("deploy page - start") { f =>
		f.driver.get(adminHost + "deploy/start")
		assert(checkPageLoad()(f.driver))
	}

	test("deploy page - generate") { f =>
		f.driver.get(adminHost + "deploy/generate")
		assert(checkPageLoad()(f.driver))
	}

	test("task page") { f =>
		f.driver.get(adminHost + "task")
		assert(checkPageLoad()(f.driver))
	}

	test("task page - run") { f =>
		f.driver.get(adminHost + "task/run/RenderTask")
		assert(checkPageLoad()(f.driver))
	}

	test("task page - pause") { f =>
		f.driver.get(adminHost + "task/pause/RenderTask")
		assert(checkPageLoad()(f.driver))
	}

	test("task page - resume") { f =>
		f.driver.get(adminHost + "task/resume/RenderTask")
		assert(checkPageLoad()(f.driver))
	}

	test("files page") { f =>
		f.driver.get(adminHost + "files/")
		assert(checkPageLoad()(f.driver))
	}

	test("logs page") { f =>
		f.driver.get(adminHost + "logs")
		assert(checkPageLoad()(f.driver))
	}

	test("logs page with severity") { f =>
		f.driver.get(adminHost + "logs?severity=Error")
		assert(checkPageLoad()(f.driver))
	}

	test("logs page heapdump") { f =>
		f.driver.get(adminHost + "logs?severity=Error")
		assert(checkPageLoad()(f.driver))
	}

	test("dbconsole page") { f =>
		f.driver.get(adminHost + "dbconsole")
		assert(checkPageLoad()(f.driver))
	}

	test("dbconsole select 1") { implicit f =>
		f.driver.get(adminHost + "dbconsole")
		val sql = f.driver.findElement(By.cssSelector("textarea"))
		sql.sendKeys("SELECT 1;")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad("#jakon_messages")(f.driver))
	}

	test("dbconsole select error") { implicit f =>
		f.driver.get(adminHost + "dbconsole")
		val sql = f.driver.findElement(By.cssSelector("textarea"))
		sql.sendKeys("SSS;")
		val submit = f.driver.findElement(By.cssSelector(".btn.btn-lg.btn-success"))
		submit.click()

		assert(checkPageLoad("#jakon_messages")(f.driver))
	}
}
