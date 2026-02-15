package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.configuration.Settings
import io.javalin.http.Context
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.server.ResourceService
import org.eclipse.jetty.server.handler.ResourceHandler

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Paths}
import scala.language.postfixOps

/**
  * Created by TPa on 03.01.26.
  */
class DevStaticFilesController  {

	// TODO: merge with StaticFilesController
	def doGet(ctx: Context): AnyRef = {
		val filePath = Settings.getStaticDir + ctx.path()
		val file = new File(filePath)

		if (file.exists() && file.isFile) {
			ctx.status(200)

			// Set content type based on file extension
			val contentType = Files.probeContentType(Paths.get(filePath))
			if (contentType != null) {
				ctx.contentType(contentType)
			}

			// Stream file content
			val inputStream = new FileInputStream(file)
			ctx.result(inputStream)
		} else {
			ctx.status(404)
		}
		ctx
	}

}
