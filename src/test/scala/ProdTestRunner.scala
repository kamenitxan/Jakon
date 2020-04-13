import java.io.{File, IOException}

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.{AnnotationScanner, Settings}
import cz.kamenitxan.jakon.core.custom_pages.AbstractStaticPage
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.Pebble
import jakon.pagelet.{JsonPageletTest, PageletTest}
import org.scalatest.{BeforeAndAfterAll, Suites}
import utils.SecurityTest
import utils.mail.EmailTest
import webui._

/**
  * Created by TPa on 27.08.16.
  */
class ProdTestRunner extends Suites(
	new AuthTest,
	new EmailTest,
	new MenuTest,
	new SecurityTest,
	new ApiTest,
	new ObjectControllerTest,
	new FileManagerTest,
	new WebUi,
	new PageletTest,
	new JsonPageletTest,
	new ObjectExtensionTest
) with BeforeAndAfterAll {

	val config = "jakonConfig=jakon_config_test_prod.properties"

	override def beforeAll(): Unit = {
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

	override def afterAll(): Unit = {
		println("After!")  // shut down the web server
		new File("jakonUnitTest.sqlite").delete()
	}

}


