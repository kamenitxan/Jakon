package jakontest

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controller.PageController
import cz.kamenitxan.jakon.core.custom_pages.AbstractStaticPage
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{Category, Page, Post}
import cz.kamenitxan.jakon.core.template.Pebble
import jakontest.core.*
import jakontest.core.functions.LinkTest
import jakontest.core.pagelet.{JsonPageletTest, PageletTest}
import jakontest.core.task.TaskRunnerTest
import jakontest.devtools.DevtoolsTest
import jakontest.logging.LoggingTest
import jakontest.utils.entity.{TestExtNoFields, TestExtUser, TestObject}
import jakontest.utils.mail.EmailTest
import jakontest.utils.{I18NUtilTest, SecurityTest, SqlGenTest, UtilsTest}
import jakontest.validation.ValidationTest
import jakontest.webui.*
import org.scalatest.{BeforeAndAfterAll, Suites}

import java.io.{File, IOException}

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
	new ObjectExtensionTest,
	new DevtoolsTest,
	new ServiceTest,
	new ValidationTest,
	new UtilsTest,
	new TaskRunnerTest
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
				app.run(Array[String](config, s"port=${(Settings.getPort + 1).toString} test_param"))
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

	override protected def afterAll(): Unit = {
		super.afterAll()
		JakonInit.javalin.stop()
	}
}

class TestJakonApp extends JakonInit {

	override def daoSetup(): Unit = {
		super.daoSetup()
		DBHelper.addDao(classOf[Category])
		DBHelper.addDao(classOf[Post])
		DBHelper.addDao(classOf[Page])
		DBHelper.addDao(classOf[TestObject])
		DBHelper.addDao(classOf[TestExtUser])
		DBHelper.addDao(classOf[TestExtNoFields])
	}

	Director.registerController(new PageController)

	override def adminControllers(): Unit = {
		super.adminControllers()
	}
}
