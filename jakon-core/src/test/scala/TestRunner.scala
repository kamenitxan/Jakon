import java.io.File

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.customPages.AbstractStaticPage
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.example.Main
import functions.LinkTest
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

		Main.main(Array[String]("jakonConfig=jakon_config_example.properties"))
		//Main.main(Array[String]("jakonConfig=jakon_config_test.properties"))

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
