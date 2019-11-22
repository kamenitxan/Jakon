package cz.kamenitxan.jakon.webui.controler


import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}


abstract class AbstractController extends CustomController {
	val template: String
	val icon: String = "fa-cog"

	def doRender(req: Request, res: Response): Context = {
		try {
			render(req, res)
		} catch {
			case e: Exception => {
				Logger.error("Exception thrown during " + name() + " controller render", e)
				new Context(Map[String, Any](
					"errors" -> List(e.toString)
				), template)
			}
		}
	}
}
