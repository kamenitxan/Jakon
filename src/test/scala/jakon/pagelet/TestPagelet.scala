package jakon.pagelet

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

	@Post(path = "/post", template = "ExamplePagelet")
	def post(req: Request, res: Response): mutable.Map[String, Any] = {
		redirect(req, res, "/pagelet/get")
	}

}
