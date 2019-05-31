package cz.kamenitxan.jakon.devtools

import java.io.File

import cz.kamenitxan.jakon.core.configuration.Settings
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.server.ResourceService
import org.eclipse.jetty.server.handler.ResourceHandler
import spark.{Request, Response}

import scala.language.postfixOps

/**
  * Created by TPa on 07.07.18.
  */
class StaticFilesController extends ResourceHandler {
	setResourceBase(Settings.getOutputDir)
	doStart()

	private val resF = this.getClass.getSuperclass.getDeclaredField("_resourceService")
	resF.setAccessible(true)
	private val resourceService = resF.get(this).asInstanceOf[ResourceService]

	def doGet(req: Request, res: Response): AnyRef = {
		val request = req.raw()
		val response = res.raw()

		if (new File(Settings.getOutputDir + req.pathInfo()).exists()) {
			res.status(200)
			if (HttpMethod.GET.is(request.getMethod)) { // try another handler
				resourceService.doGet(request, response)
			}
		}
		res.body()
	}
}
