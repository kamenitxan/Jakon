package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.entity.Message
import spark.{Request, Response}

import scala.collection.mutable

/**
  * Created by TPa on 30.04.18.
  */
object PageContext {
	val context: ThreadLocal[PageContext] = new ThreadLocal[PageContext]

	def init(req: Request, res: Response): PageContext = {
		if (PageContext.context.get() != null) {
			throw new IllegalStateException("PageContext already initialized")
		}
		val ctx = new PageContext(req, res)
		context.set(ctx)
		ctx
	}

	def getInstance(): PageContext = {
		PageContext.context.get()
	}

	def destroy(): Unit = {
		context.remove()
	}
}

case class PageContext(req: Request, res: Response) {
	val messages = new mutable.MutableList[Message]()

	def getLoggedUser: Option[JakonUser] = {
		Option.apply(req.session.attribute("user"))
	}
}
