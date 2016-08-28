import cz.kamenitxan.jakon.core.customPages.StaticPage
import cz.kamenitxan.jakon.core.template.Pebble
import cz.kamenitxan.jakon.core.{Director, Settings}
import org.scalatest.{BeforeAndAfterAll, Suites}

/**
  * Created by TPa on 27.08.16.
  */
class TestRunner extends Suites(new RenderTest) with BeforeAndAfterAll {

	override def beforeAll() {
		println("Before!")
		Director.init()
		Settings.init(null)
		Settings.setTemplateEngine(new Pebble)
		val staticPage = new StaticPage("staticPage", "static")
		Director.registerCustomPage(staticPage)

		Director.render()
	}

	override def afterAll() {
		println("After!")  // shut down the web server
	}
}
