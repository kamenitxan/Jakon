import java.io.File

import cz.kamenitxan.jakon.core.customPages.StaticPage
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonUser, Page}
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.{Director, Settings}
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
		val staticPage = new StaticPage("staticPage", "static")
		Director.registerCustomPage(staticPage)
		DBHelper.addDao(classOf[JakonUser])

		val page = new Page
		page.setTitle("test page 1")
		page.setContent("test content")
		page.setUrl("/page/testpage1")
		DBHelper.getPageDao.createIfNotExists(page)

		Director.render()
	}

	override def afterAll() {
		println("After!")  // shut down the web server
		new File("JakonTest.sqlite").delete()
	}
}
