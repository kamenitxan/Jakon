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
			PageletInitializer.initControllers(Seq(classOf[TestValidationPagelet], classOf[TestPathParamPagelet]))
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

	test("valid data are passed to pagelet with path param") {
		val resp = browser.post("/cs/langPagelet/save", "name" -> "abc", "count" -> "3")
		assert(resp.body() == "saved-cs-abc")
	}

	test("invalid data redirect back to the form with path params replaced") {
		val resp = browser.post("/cs/langPagelet/save", "name" -> "", "count" -> "3")
		assert(resp.statusCode() == 302)
		assert(location(resp) == "/cs/langPagelet/save")
	}

	test("path param value is URL encoded in redirect after invalid data") {
		val resp = browser.post("/a%20b%2Fc/langPagelet/save", "name" -> "", "count" -> "3")
		assert(resp.statusCode() == 302)
		assert(location(resp) == "/a%20b%2Fc/langPagelet/save")
	}

	test("replacePathParams replaces only whole {key} and <key> placeholders") {
		assert(PageletInitializer.replacePathParams("/{lang}/language/save", Map("lang" -> "cs")) == "/cs/language/save")
		assert(PageletInitializer.replacePathParams("/{lang}/{id}", Map("lang" -> "cs", "id" -> "5")) == "/cs/5")
		assert(PageletInitializer.replacePathParams("/files/<path>", Map("path" -> "a b/c")) == "/files/a%20b/c")
		assert(PageletInitializer.replacePathParams("/x/{v}", Map("v" -> "č&?#+")) == "/x/%C4%8D%26%3F%23%2B")
		assert(PageletInitializer.replacePathParams("/static", Map.empty) == "/static")
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
