package cz.kamenitxan.jakon.webui.controler.impl

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.deploy.DeployDirector
import cz.kamenitxan.jakon.core.deploy.entity.Server
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.controler.{AbstractController, ExecuteFun}
import net.liftweb.json.{DefaultFormats, JsonParser}
import spark.route.HttpMethod
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.io.Source


class DeployControler extends AbstractController {
	override val template: String = "pages/deploy"
	override val icon: String = "fa-server"

	@ExecuteFun(path = "deploy/start", method = HttpMethod.get)
	def deploy(req: Request, res: Response): Context = {
		DeployDirector.deploy()
		res.redirect("/admin/deploy")
		new Context(Map[String, Any](), template)
	}

	@ExecuteFun(path = "deploy/generate", method = HttpMethod.get)
	def generate(req: Request, res: Response): Context = {
		Director.render()
		res.redirect("/admin/deploy")
		new Context(Map[String, Any](), template)
	}

	override def render(req: Request, res: Response): Context = {
		new Context(Map[String, Any](
			"deployMode" -> Settings.getDeployMode,
			"deployType" -> Settings.getDeployType,
			"servers" -> DeployDirector.servers.asJava
		), template)
	}

	override def name(): String = "DEPLOY"

	override def path(): String = "deploy"
}
