package cz.kamenitxan.jakon.core.dynamic

import spark.{Request, Response, TemplateEngine}

import scala.collection.mutable

trait IPagelet {

	val engine: TemplateEngine

	def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String

	def redirect(req: Request, res: Response, target: String): mutable.Map[String, Any]

	def redirect(req: Request, res: Response, target: String, requestParams: Map[String, Any]): mutable.Map[String, Any]
}
