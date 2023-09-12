package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}

/**
  * Created by TPa on 06.09.16.
  */
object Dashboard {
	def getDashboard(req: Request, res: Response): Context = {
		new Context(Map[String, Any](
			"pathInfo" -> "/admin/index"
		), "logged")
	}
}
