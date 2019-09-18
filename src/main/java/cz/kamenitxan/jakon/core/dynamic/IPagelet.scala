package cz.kamenitxan.jakon.core.dynamic

import spark.{Request, Response}

import scala.collection.mutable

trait IPagelet {

	def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String

	def redirect(req: Request, res: Response, target: String): mutable.Map[String, Any]

	def redirect(req: Request, res: Response, target: String, requestParams: AnyRef): mutable.Map[String, Any]
}
