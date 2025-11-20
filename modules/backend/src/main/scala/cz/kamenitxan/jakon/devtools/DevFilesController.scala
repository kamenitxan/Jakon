package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.configuration.Settings
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.server.ResourceService
import org.eclipse.jetty.server.handler.ResourceHandler

import java.io.File
import scala.language.postfixOps

/**
  * Created by TPa on 07.07.18.
  */
class DevFilesController extends ResourceHandler {
	setResourceBase("src/main/resources/static")
	doStart()

	private val resF = this.getClass.getSuperclass.getDeclaredField("_resourceService")
	resF.setAccessible(true)
	private val resourceService = resF.get(this).asInstanceOf[ResourceService]

	def doGet(ctx: Context): AnyRef = {
		val request = ctx.req()
		val response = ctx.res()

		if (new File("src/main/resources/static" + ctx.path()).exists()) {
			ctx.status(200)
			if (HttpMethod.GET.is(request.getMethod)) { // try another handler
				resourceService.doGet(request, response)
			}
		} else {
			ctx.status(404)
		}
		ctx
	}
}
