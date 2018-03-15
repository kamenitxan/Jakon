package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}

/**
  * Created by TPa on 06.09.16.
  */
object Dashboard {
	def getDashboard(req: Request, res: Response) = {
		new Context(Map[String, Any](), "logged")
	}
}
