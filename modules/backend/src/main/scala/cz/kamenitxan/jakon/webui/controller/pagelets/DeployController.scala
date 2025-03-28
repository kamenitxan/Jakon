package cz.kamenitxan.jakon.webui.controller.pagelets

import cz.kamenitxan.jakon.core.Director
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.deploy.DeployDirector
import cz.kamenitxan.jakon.core.dynamic.{Get, Pagelet}
import io.javalin.http.Context

import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*


@Pagelet(path = "/admin/deploy", showInAdmin = true)
class DeployController extends AbstractAdminPagelet {

	override val name: String = this.getClass.getSimpleName
	override val icon: String = "fa-server"

	val template: String = "pages/deploy"

	@Get(path = "/start", template = "pages/deploy")
	def deploy(ctx: Context): Unit = {
		DeployDirector.deploy()
		redirect(ctx, "/admin/deploy")
	}

	@Get(path = "/generate", template = "pages/deploy")
	def generate(ctx: Context): Unit = {
		Director.render()
		redirect(ctx, "/admin/deploy")
	}

	@Get(path = "", template = "pagelet/task")
	def render(ctx: Context): mutable.Map[String, Any] = {
		val servers = if (Files.exists(Paths.get("servers.json"))) {
			DeployDirector.servers
		} else {
			Seq.empty
		}
		mutable.Map[String, Any](
			"deployMode" -> Settings.getDeployMode,
			"deployType" -> Settings.getDeployType,
			"servers" -> servers.asJava
		)
	}

}
