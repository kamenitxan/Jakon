package jakon.pagelet

import com.google.gson.Gson
import cz.kamenitxan.jakon.TestJsonPagelet
import cz.kamenitxan.jakon.core.dynamic.JsonPageletInitializer
import cz.kamenitxan.jakon.core.dynamic.entity.ResponseStatus
import jakon.pagelet.entity.GetResponse
import test.TestBase

/**
 * Created by TPa on 13.04.2020.
 */
class JsonPageletTest extends TestBase {

	private val gson = new Gson()

	test("example json pagelet - get") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + "/jsonPagelet/get"
		f.driver.get(url)

		val resp = gson.fromJson(f.driver.getPageSource, classOf[GetResponse])

		assert(resp.status == ResponseStatus.success)
		assert(resp.data == "string")
	}

	test("example json pagelet - throw") { f =>
		JsonPageletInitializer.initControllers(Seq(classOf[TestJsonPagelet]))

		val url = host + "/jsonPagelet/throw"
		f.driver.get(url)

		assert(f.driver.getPageSource.contains("IllegalAccessException"))
	}

}
