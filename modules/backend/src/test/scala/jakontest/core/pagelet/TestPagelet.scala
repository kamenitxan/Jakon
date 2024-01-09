package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import spark.{Request, Response}

import scala.collection.mutable

@Pagelet(path = "pagelet")
class TestPagelet extends AbstractPagelet {

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(req: Request, res: Response): mutable.Map[String, Any] = {
		mutable.Map(
			"pushed" -> "pushedValue"
		)
	}

	@Post(path = "/post", template = "pagelet/ExamplePagelet")
	def post(req: Request, res: Response): mutable.Map[String, Any] = {
		redirect(req, res, "/pagelet/get")
	}

	@Post(path = "/stringPost", template = "")
	def post2(req: Request, res: Response): String = {
		"StringResponse"
	}

}
