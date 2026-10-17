package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.PageletInitializer
import jakontest.test.HttpBrowser
import jakontest.test.HttpBrowser.location
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

/** Validation of pagelet POST data and other pagelet request flows. */
@DoNotDiscover
class PageletValidationTest extends AnyFunSuite {

	private var initialized = false

	private def browser: HttpBrowser = {
		if (!initialized) {
			PageletInitializer.initControllers(Seq(classOf[TestValidationPagelet]))
			initialized = true
		}
		new HttpBrowser
	}

	test("valid data are passed to pagelet method") {
		val resp = browser.post("/validationPagelet/save", "name" -> "abc", "count" -> "3")
		assert(resp.body() == "saved-abc-3")
	}

	test("invalid data redirect back to the form") {
		val resp = browser.post("/validationPagelet/save", "name" -> "", "count" -> "3")
		assert(resp.statusCode() == 302)
		assert(location(resp).startsWith("/validationPagelet/save"))
	}

	test("validate=true returns validation result as JSON") {
		val invalid = browser.post("/validationPagelet/save?validate=true", "name" -> "", "count" -> "3")
		assert(invalid.body().contains("TestValidatedData_name"))
		val valid = browser.post("/validationPagelet/save?validate=true", "name" -> "abc", "count" -> "3")
		assert(valid.body() == "true")
	}

	test("missing primitive values get default values") {
		val resp = browser.get("/validationPagelet/primitives")
		assert(resp.body().contains("TestPrimitiveData(false,"))
		assert(resp.body().contains("0,0,0,0,0.0,0.0)"))
	}

	test("pagelet redirect skips rendering") {
		val resp = browser.get("/validationPagelet/redirect")
		assert(resp.statusCode() == 302)
		assert(location(resp) == "/pagelet/get")
	}

	test("missing template of pagelet is an error") {
		val resp = browser.get("/validationPagelet/brokenTemplate")
		assert(resp.statusCode() != 200 || resp.body().isEmpty)
	}
}
