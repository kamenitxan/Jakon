package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.configuration.Settings
import io.javalin.http.Context

import java.io.FileInputStream
import java.nio.file.Files
import scala.language.postfixOps

/**
  * Created by TPa on 07.07.18.
  */
class StaticFilesController {

	def doGet(ctx: Context): AnyRef = {
		StaticFilesController.serveFile(ctx, Settings.getOutputDir)
	}
}

object StaticFilesController {

	def serveFile(ctx: Context, baseDir: String): AnyRef = {
		SafeFileResolver.resolve(baseDir, ctx.path()) match {
			case Some(file) =>
				ctx.status(200)

				// Set content type based on file extension
				val contentType = Files.probeContentType(file.toPath)
				if (contentType != null) {
					ctx.contentType(contentType)
				}

				// Stream file content
				val inputStream = new FileInputStream(file)
				ctx.result(inputStream)
			case None =>
				ctx.status(404)
		}
		ctx
	}
}
