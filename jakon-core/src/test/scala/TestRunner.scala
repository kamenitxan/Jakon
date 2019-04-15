import java.io.File

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.customPages.AbstractStaticPage
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.core.template.Pebble
import functions.LinkTest
import jakon.RenderTest
import org.scalatest.{BeforeAndAfterAll, Suites}
import utils.AesEncryptorTest
import webui._

/**
  * Created by TPa on 27.08.16.
  */
class TestRunner extends Suites(
	new RenderTest,
	new LinkTest,
	new AuthTest,
	//new EmailTest,
	new MenuTest,
	new AesEncryptorTest,
	new ApiTest,
	new ObjectControllerTest,
	new FileManagerTest
) with BeforeAndAfterAll {

	override def beforeAll() {

		new File("jakonUnitTest.sqlite").delete()
		println("Before!")
		Director.init()
		Settings.setTemplateEngine(new Pebble)

		val app = new TestJakonApp()
		app.run(Array[String]("jakonConfig=jakon_config_test.properties"))

		val staticPage = new AbstractStaticPage("staticPage", "static") {}
		Director.registerCustomPage(staticPage)


		val page = new Page
		page.title = "test page 1"
		page.content = "test content"
		page.setUrl("/page/testpage1")
		page.create()

		Thread.sleep(1000)
		Director.render()
	}

	override def afterAll() {
		println("After!")  // shut down the web server
		new File("jakonUnitTest.sqlite").delete()
	}
}

class TestJakonApp extends JakonInit{

	daoSetup = () => {
		DBHelper.addDao(classOf[Category])
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Page])
	}

	Director.registerControler(new PageControler)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
