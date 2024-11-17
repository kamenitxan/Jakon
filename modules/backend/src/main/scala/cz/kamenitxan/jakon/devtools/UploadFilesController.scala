package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.model.FileType

import java.io.{File, FileInputStream}
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import io.javalin.http.Context
import org.apache.commons.io.IOUtils

import scala.language.postfixOps

/**
  * Fallback for missing nginx upload directory configuration in debug mode
  * Created by TPa on 07.07.18.
  */
class UploadFilesController  {

	def doGet(ctx: Context): String = {
		val fileName = ctx.path().replace("/upload", FileManagerController.REPOSITORY_BASE_PATH + "/basePath")
		val file = new File(fileName)
		if (file.exists()) {
			ctx.status(200)

			FileManagerController.getFileType(file.toPath) match {
				case FileType.FILE => ctx.header("Content-Type", "text/html")
			}

			val os = ctx.res().getOutputStream
			val is = new FileInputStream(file)

			IOUtils.copy(is, os)
			os.close()
			is.close()
			ctx.header("Content-Length", file.length().toString)
		}
		ctx.body()
	}
}
