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
		if (req == null || excludedPaths.exists(path => req.pathInfo().startsWith(path))) {
			return null
		}
		val ctx = new PageContext(req, res)
		context.set(ctx)
		if (req.session().attribute(MESSAGES_KEY) != null) {
			req.session().attribute(MESSAGES_KEY).asInstanceOf[mutable.ArrayDeque[Message]].foreach(m => {
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
	val messages = new mutable.ArrayDeque[Message]()

	def getLoggedUser: Option[JakonUser] = {
		val user: JakonUser = req.session.attribute("user")
		Option.apply(user)
	}

	def addMessage(severity: MessageSeverity, value: String): Unit = {
		messages += new Message(severity, value)
	}
}
