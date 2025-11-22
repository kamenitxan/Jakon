package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import io.javalin.http.Context

import scala.collection.mutable

@Pagelet(path = "pagelet")
class TestPagelet extends AbstractPagelet {

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "pushedValue"
		)
	}

	@Post(path = "/post", template = "pagelet/ExamplePagelet")
	def post(ctx: Context): mutable.Map[String, Any] = {
		redirect(ctx, "/pagelet/get")
		null
	}

	@Post(path = "/stringPost", template = "")
	def post2(): String = {
		"StringResponse"
	}

}
