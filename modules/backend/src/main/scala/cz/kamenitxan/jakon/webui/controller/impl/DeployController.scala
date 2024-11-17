package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.deploy.DeployDirector
import cz.kamenitxan.jakon.webui.HttpMethod
import cz.kamenitxan.jakon.webui.controller.{AbstractController, ExecuteFun}
import io.javalin.http.Context

import scala.jdk.CollectionConverters.*


class DeployController extends AbstractController {
	override val template: String = "pages/deploy"
	override val icon: String = "fa-server"

	@ExecuteFun(path = "deploy/start", method = HttpMethod.get)
	def deploy(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		DeployDirector.deploy()
		ctx.redirect("/admin/deploy")
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), template)
	}

	@ExecuteFun(path = "deploy/generate", method = HttpMethod.get)
	def generate(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		Director.render()
		ctx.redirect("/admin/deploy")
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), template)
	}

	override def render(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
			"deployMode" -> Settings.getDeployMode,
			"deployType" -> Settings.getDeployType,
			"servers" -> DeployDirector.servers.asJava
		), template)
	}

	override def name(): String = "DEPLOY"

	override def path(): String = "deploy"
}
