package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{Request, Response}

import scala.collection.mutable

/**
  * Created by TPa on 30.04.18.
  */
object PageContext {
	val MESSAGES_KEY = "messages_session_key"
	val context: ThreadLocal[PageContext] = new ThreadLocal[PageContext]

	val excludedPaths = List("/favicon.ico", "/css", "/js", "/jakon", "/vendor")
	def init(req: Request, res: Response): PageContext = {
		if (PageContext.context.get() != null) {
			throw new IllegalStateException("PageContext already initialized")
		}
		if (req == null || excludedPaths.exists(path => path.startsWith(req.pathInfo()))) {
			return null
		}
		val ctx = new PageContext(req, res)
		context.set(ctx)
		if (req.session().attribute(MESSAGES_KEY) != null) {
			req.session().attribute(MESSAGES_KEY).asInstanceOf[mutable.MutableList[Message]].foreach(m => {
				context.get().messages += m
			})
			req.session().removeAttribute(MESSAGES_KEY)
		}
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

	def addMessage(severity: MessageSeverity, value: String): Unit = {
		messages += new Message(severity, value)
	}
}
