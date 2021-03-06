package cz.kamenitxan.jakon.webui.controller

import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}

trait CustomController {
	def render(req: Request, res: Response): Context

	def name(): String

	def path(): String
}
