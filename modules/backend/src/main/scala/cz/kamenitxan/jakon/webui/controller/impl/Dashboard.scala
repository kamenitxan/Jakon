package cz.kamenitxan.jakon.webui.controller.impl

import io.javalin.http.Context

/**
  * Created by TPa on 06.09.16.
  */
object Dashboard {
	def getDashboard(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
			"pathInfo" -> "/admin/index"
		), "logged")
	}
}
