package cz.kamenitxan.jakon.webui.controler.objectextension

import cz.kamenitxan.jakon.core.dynamic.Get
import spark.{Request, Response}

import scala.collection.mutable

@ObjectExtension
class JakonUserExtension extends AbstractObjectExtension {

	override def render(context: mutable.Map[String, Any], templatePath: String, req: Request): String = {
		super.render(context, templatePath, req)
	}

	@Get(path = "/resetPasswordStep2", template = "")
	def get(req: Request, res: Response): Unit = {

	}

}
