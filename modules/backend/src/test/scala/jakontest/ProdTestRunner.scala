package jakontest

import jakontest.core.pagelet.{JsonPageletTest, PageletTest}
import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.custom_pages.AbstractStaticPage
import cz.kamenitxan.jakon.core.model.Page
import cz.kamenitxan.jakon.core.template.Pebble
import org.scalatest.{BeforeAndAfterAll, Suites}
import jakontest.utils.SecurityTest
import jakontest.utils.mail.EmailTest
import jakontest.webui._

import java.io.{File, IOException}

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
		new File("jakonUnitTest.sqlite").delete()
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


