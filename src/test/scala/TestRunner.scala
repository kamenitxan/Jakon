import java.io.File

import cz.kamenitxan.jakon.core.customPages.StaticPage
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.example.Main
import functions.LinkTest
import org.scalatest.{BeforeAndAfterAll, Suites}
import webui.AuthTest

/**
  * Created by TPa on 27.08.16.
  */
class TestRunner extends Suites(new RenderTest, new LinkTest, new AuthTest) with BeforeAndAfterAll {

	override def beforeAll() {
		println("Before!")
		Director.init()
		Settings.init(null)
		Settings.setTemplateEngine(new Pebble)
		Settings.setDeployMode(DeployMode.PRODUCTION)

		Main.main(Array[String]())

		val staticPage = new StaticPage("staticPage", "static") {}
		Director.registerCustomPage(staticPage)


		val page = new Page
		page.title = "test page 1"
		page.content = "test content"
		page.setUrl("/page/testpage1")
		//DBHelper.getPageDao.createIfNotExists(page)

		Director.render()
	}

	override def afterAll() {
		println("After!")  // shut down the web server
		new File("JakonTest.sqlite").delete()
	}
}
