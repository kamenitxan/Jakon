package cz.kamenitxan.jakon.webui.controler


import cz.kamenitxan.jakon.webui.Context
import org.slf4j.LoggerFactory
import spark.{Request, Response}


abstract class AbstractController extends CustomController {
	private val logger = LoggerFactory.getLogger(this.getClass)
	val template: String
	val icon: String = "fa-cog"

	def doRender(req: Request, res: Response): Context = {
		try {
			render(req, res)
		} catch {
			case e: Exception => {
				logger.error("Exception thrown during " + name() + " controller render", e)
				new Context(Map[String, Any](
					"errors" -> List(e.toString)
				), template)
			}
		}
	}
}
