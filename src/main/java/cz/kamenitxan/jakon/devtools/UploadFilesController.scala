package cz.kamenitxan.jakon.devtools

import java.io.{File, FileInputStream}

import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import org.apache.commons.io.IOUtils
import spark.{Request, Response}

import scala.language.postfixOps

/**
  * Fallback for missing nginx upload directory configuration in debug mode
  * Created by TPa on 07.07.18.
  */
class UploadFilesController  {

	def doGet(req: Request, res: Response): String = {
		val fileName = req.pathInfo().replace("/upload", FileManagerController.REPOSITORY_BASE_PATH + "/basePath")
		val file = new File(fileName)
		if (file.exists()) {
			res.status(200)
			//res.`type`("image/" + fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length))
			val os = res.raw().getOutputStream
			val is = new FileInputStream(file)

			IOUtils.copy(is, os)
			os.close()
			is.close()
		}
		res.body()
	}
}
