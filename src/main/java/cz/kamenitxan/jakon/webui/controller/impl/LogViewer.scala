package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.logging._
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controller.AbstractController
import spark.{Request, Response}

import scala.jdk.CollectionConverters._

class LogViewer extends AbstractController {
	override val template: String = "pages/logViewer"
	override val icon: String = "fa-exclamation-triangle"

	override def name(): String = "LOGS"

	override def path(): String = "logs"

	override def render(req: Request, res: Response): Context = {
		new Context(Map[String, Any](
			"logs" -> LogService.getLogs.reverse.asJava,
			"severities" -> Seq(Debug, Info, Warning, cz.kamenitxan.jakon.logging.Error, Critical)
		), template)
	}
}
