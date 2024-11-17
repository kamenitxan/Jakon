package cz.kamenitxan.jakon.webui.controller

import io.javalin.http.Context


@Deprecated
trait CustomController {
	def render(ctx: Context): cz.kamenitxan.jakon.webui.Context

	def name(): String

	def path(): String
}
