package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}
import spark.{Request, Response}

import scala.collection.mutable

/**
 * Created by TPa on 23.06.2021.
 */
@Pagelet(path = "/jakon")
class HealthCheckPagelet extends AbstractPagelet {

	@Get(path = "/health", template = "ExamplePagelet")
	def get(req: Request, res: Response): Unit = {

	}

	override def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		"JAKON_OK"
	}
}
