package cz.kamenitxan.jakon.core.dynamic


import io.javalin.http.Context
import io.javalin.rendering.FileRenderer

import scala.collection.mutable

trait IPagelet {

	val engine: FileRenderer

	def render(context: mutable.Map[String, Any], templatePath: String, ctx: Context): String

	def redirect(ctx: Context, target: String): Unit

	def redirect(ctx: Context, target: String, requestParams: Map[String, Any]): Unit
}
