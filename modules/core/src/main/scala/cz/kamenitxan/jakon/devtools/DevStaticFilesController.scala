package cz.kamenitxan.jakon.devtools

import cz.kamenitxan.jakon.core.configuration.Settings
import io.javalin.http.Context

import scala.language.postfixOps

/**
  * Created by TPa on 03.01.26.
  */
class DevStaticFilesController  {

	def doGet(ctx: Context): AnyRef = {
		StaticFilesController.serveFile(ctx, Settings.getStaticDir)
	}

}
