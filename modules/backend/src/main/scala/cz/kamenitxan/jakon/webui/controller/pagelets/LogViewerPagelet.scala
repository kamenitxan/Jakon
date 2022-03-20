package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import cz.kamenitxan.jakon.logging._
import spark.{Request, Response}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
 * Created by TPa on 15.03.2022.
 */
@Pagelet(path = "/admin/logs", showInAdmin = true)
class LogViewerPagelet extends AbstractAdminPagelet {
	override val name: String = this.getClass.getSimpleName
	override val icon: String = "fa-exclamation-triangle"


	@Get(path = "", template = "pagelet/logViewer")
	def render(req: Request, res: Response): mutable.Map[String, Any] = {
		mutable.Map[String, Any](
			"logs" -> LogService.getLogs.reverse.asJava,
			"severities" -> Seq(Debug, Info, Warning, cz.kamenitxan.jakon.logging.Error, Critical).asJava
		)
	}
}
