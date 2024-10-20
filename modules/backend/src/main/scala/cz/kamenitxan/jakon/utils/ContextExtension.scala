package cz.kamenitxan.jakon.utils

import io.javalin.http.Context
import io.javalin.plugin.ContextPlugin

import scala.jdk.CollectionConverters.*

/**
 * Created by Kamenitxan on 20.10.2024
 */
class ContextExtension extends ContextPlugin[ExtConfig, ExtendedContext] {

	override def createExtension(context: Context): ExtendedContext = {
		ExtendedContext(context, this)
	}
}

class ExtendedContext(val ctx: Context, private val plugin: ContextExtension) {

	def pathParamOpt(path: String): Option[String] = {
		ctx.pathParamMap().asScala.get(path)
	}
}

class ExtConfig {

}