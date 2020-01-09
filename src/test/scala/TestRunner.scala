import java.io.{File, IOException}

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controler.PageControler
import cz.kamenitxan.jakon.core.customPages.AbstractStaticPage
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.core.template.Pebble
import functions.LinkTest
import jakon.pagelet.PageletTest
import jakon.{DeployTest, ModelTest, RenderTest, SettingsTest}
import logging.LoggingTest
import org.scalatest.{BeforeAndAfterAll, Suites}
import utils.mail.EmailTest
import utils.{SecurityTest, SqlGenTest, TestObject, i18nUtilTest}
import webui._

/**
  * Created by TPa on 27.08.16.
  */
class TestRunner extends Suites(
	new RenderTest,
	new LinkTest,
	new AuthTest,
	new EmailTest,
	new MenuTest,
	new SecurityTest,
	new ApiTest,
	new ObjectControllerTest,
	new FileManagerTest,
	new WebUi,
	new SqlGenTest,
	new ModelTest,
	new SettingsTest,
	new i18nUtilTest,
	new PageletTest,
	new DeployTest,
	new LoggingTest
) with BeforeAndAfterAll {

	override def beforeAll() {

		new File("jakonUnitTest.sqlite").delete()
		println("Before!")
		Director.init()
		Settings.setTemplateEngine(new Pebble)

		val app = new TestJakonApp()
		try {
			app.run(Array[String]("jakonConfig=jakon_config_test.properties"))
		} catch {
			case _: IOException =>
				app.run(Array[String]("jakonConfig=jakon_config_test.properties", s"port=${(Settings.getPort + 1).toString}"))
		}

		val staticPage = new AbstractStaticPage("staticPage", "static") {}
		Director.registerCustomPage(staticPage)


		val page = new Page
		page.title = "test page 1"
		page.content = "test content"
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

	override def daoSetup() = {
		DBHelper.addDao(classOf[Category])
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Page])
		DBHelper.addDao(classOf[TestObject])
	}

	Director.registerControler(new PageControler)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
