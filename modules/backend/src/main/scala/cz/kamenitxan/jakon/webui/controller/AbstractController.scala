package cz.kamenitxan.jakon.webui.controller

import io.javalin.http.Context
import cz.kamenitxan.jakon.logging.Logger


@Deprecated
abstract class AbstractController extends CustomController {
	val template: String
	val icon: String = "fa-cog"

	def doRender(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		try {
			render(ctx)
		} catch {
			case e: Exception => {
				Logger.error("Exception thrown during " + name() + " controller render", e)
				new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
					"errors" -> List(e.toString)
				), template)
			}
		}
	}
}
