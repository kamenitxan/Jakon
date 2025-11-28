package cz.kamenitxan.jakon.webui

import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.controller.impl.Dashboard
import cz.kamenitxan.jakon.webui.controller.objectextension.AbstractObjectExtension
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import io.javalin.http.Context

import scala.collection.mutable

object AdminSettings {
	var dashboardController: (Context) => cz.kamenitxan.jakon.webui.Context = (ctx: Context) => Dashboard.getDashboard(ctx)
	var enableFiles = true

	val customControllersInfo = new mutable.ListBuffer[CustomControllerInfo]
	val objectExtensions = new mutable.HashMap[Class[_ <: JakonObject], mutable.Set[Class[_ <: AbstractObjectExtension]]]() with mutable.MultiMap[Class[_ <: JakonObject], Class[_ <: AbstractObjectExtension]]
	
	def setDashboardController(fun: (Context) => cz.kamenitxan.jakon.webui.Context): Unit = {
		dashboardController = (ctx: Context) => {
			val result: cz.kamenitxan.jakon.webui.Context = fun(ctx)
			result.getModel.put("pathInfo", ctx.path())
			result
		}
	}
}
