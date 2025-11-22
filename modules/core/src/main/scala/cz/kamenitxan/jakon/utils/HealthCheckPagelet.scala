package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.core.dynamic.{AbstractPagelet, Get, Pagelet}
import io.javalin.http.Context

import scala.collection.mutable

/**
 * Created by TPa on 23.06.2021.
 */
@Pagelet(path = "/jakon")
class HealthCheckPagelet extends AbstractPagelet {

	@Get(path = "/health", template = "ExamplePagelet")
	def get(): Unit = {
		// just render
	}

	override def render(context: mutable.Map[String, Any], templatePath: String, ctx: Context): String = {
		"JAKON_OK"
	}
}
