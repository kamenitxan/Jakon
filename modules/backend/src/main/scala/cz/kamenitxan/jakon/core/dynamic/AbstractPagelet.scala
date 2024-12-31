package cz.kamenitxan.jakon.core.dynamic

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.FileLoader
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.pebble.PebbleExtension
import cz.kamenitxan.jakon.utils.PageContext
import io.javalin.http.Context
import io.javalin.rendering.FileRenderer
import io.javalin.rendering.template.JavalinPebble

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/**
  * Created by tomaspavel on 29.5.17.
  */
abstract class AbstractPagelet extends IPagelet {
	val loader = new FileLoader
	loader.setPrefix(Settings.getTemplateDir)
	loader.setSuffix(".peb")
	private val builder = new PebbleEngine.Builder()
	builder.loader(loader)
	builder.extension(new PebbleExtension)
	builder.strictVariables(true)
	if (DeployMode.PRODUCTION != Settings.getDeployMode) {
		builder.templateCache(null)
		builder.tagCache(null)
		builder.cacheActive(false)
	}
	val engine: FileRenderer = new JavalinPebble(builder.build())



	def render(context: mutable.Map[String, Any], templatePath: String, javalinCtx: Context): String = {
		val ctx = if (context != null) context else mutable.Map[String, Any]()

		ctx += "jakon_messages" -> PageContext.getInstance().messages.asJava
		val rp = javalinCtx.queryParamMap()
		val srp = javalinCtx.sessionAttribute(AbstractPagelet.REQUEST_PARAMS).asInstanceOf[java.util.Map[String, java.util.List[String]]]
		val mergedRp = if (srp != null) {
			srp.putAll(rp)
		} else {
			rp
		}

		ctx += AbstractPagelet.REQUEST_PARAMS -> mergedRp
		engine.render(templatePath, ctx.asJava, javalinCtx)
	}

	def redirect(ctx: Context, target: String): Unit = {
		redirect(ctx, target, null)
	}

	def redirect(ctx: Context, target: String, requestParams: Map[String, Any]): Unit = {
		ctx.sessionAttribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		ctx.sessionAttribute(AbstractPagelet.REQUEST_PARAMS, requestParams.asJava)
		ctx.redirect(target)
	}
}

object AbstractPagelet {
	val REQUEST_PARAMS = "_RP"
}
