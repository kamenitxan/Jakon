import java.io.{File, IOException}

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controller.PageController
import cz.kamenitxan.jakon.core.custom_pages.AbstractStaticPage
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.core.template.Pebble
import functions.LinkTest
import jakon.pagelet.{JsonPageletTest, PageletTest}
import jakon.{DeployTest, ModelTest, RenderTest, SettingsTest}
import logging.LoggingTest
import org.scalatest.{BeforeAndAfterAll, Suite, Suites}
import utils.entity.{TestExtUser, TestObject}
import utils.mail.EmailTest
import utils.{I18NUtilTest, SecurityTest, SqlGenTest}
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
	new I18NUtilTest,
	new PageletTest,
	new JsonPageletTest,
	new DeployTest,
	new LoggingTest,
	new ObjectExtensionTest
) with BeforeAndAfterAll {

	val config = "jakonConfig=jakon_config_test_dev.properties"

	override def beforeAll(): Unit = {
		new File("jakonUnitTest.sqlite").delete()
		println("Before!")
		Director.init()
		Settings.setTemplateEngine(new Pebble)

		val app = new TestJakonApp()
		try {
			app.run(Array[String](config))
		} catch {
			case _: IOException =>
				app.run(Array[String](config, s"port=${(Settings.getPort + 1).toString}"))
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

}

class TestJakonApp extends JakonInit {

	override def daoSetup() = {
		DBHelper.addDao(classOf[Category])
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Page])
		DBHelper.addDao(classOf[TestObject])
		DBHelper.addDao(classOf[TestExtUser])
	}

	Director.registerController(new PageController)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
