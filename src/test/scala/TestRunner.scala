import org.scalatest.{BeforeAndAfterAll, Suites}

/**
  * Created by TPa on 27.08.16.
  */
class Runner extends Suites(new RenderTest) with BeforeAndAfterAll {

	def beforeAll(configMap: Map[String, Any]) {
		println("Before!")  // start up your web server or whatever
	}

	def afterAll(configMap: Map[String, Any]) {
		println("After!")  // shut down the web server
	}
}
