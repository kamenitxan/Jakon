package example.dynamic

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet, Post}
import org.slf4j.LoggerFactory
import spark.{Request, Response}

import scala.collection.mutable

@Pagelet(path = "/authorized", authRequired = true)
class AuthorizedPagelet extends AbstractPagelet {

	@Get(path = "/get", template = "pagelet/examplePagelet")
	def get(req: Request, res: Response) = {
		val context = mutable.Map[String, Any](
			"pushed" -> "authorized"
		)
		context
	}


}
