package cz.kamenitxan.jakon.utils

import cz.kamenitxan.jakon.core.model.JakonUser
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context
import jakarta.servlet.DispatcherType

import scala.collection.mutable

/**
  * Created by TPa on 30.04.18.
  */
object PageContext {
	val MESSAGES_KEY = "messages_session_key"
	val context: ThreadLocal[PageContext] = new ThreadLocal[PageContext]

	val excludedPaths = List("/favicon.ico", "/css", "/js", "/jakon", "/vendor")

	def init(javalinContext: Context): PageContext = {
		if (PageContext.context.get() != null && javalinContext.req.getDispatcherType != DispatcherType.FORWARD) {
			throw new IllegalStateException("PageContext already initialized")
		}
		if (javalinContext == null || excludedPaths.exists(path => javalinContext.path().startsWith(path))) {
			return null
		}
		val ctx = new PageContext(javalinContext)
		context.set(ctx)
		if (javalinContext.sessionAttribute(MESSAGES_KEY) != null) {
			javalinContext.sessionAttribute(MESSAGES_KEY).asInstanceOf[mutable.ArrayDeque[Message]].foreach(m => {
				context.get().messages += m
			})
			javalinContext.sessionAttribute(MESSAGES_KEY, null)
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

case class PageContext(ctx: Context) {
	val messages = new mutable.ArrayDeque[Message]()

	def getLoggedUser: Option[JakonUser] = {
		val user: JakonUser = ctx.sessionAttribute("user")
		Option.apply(user)
	}

	def addMessage(severity: MessageSeverity, value: String): Unit = {
		messages += new Message(severity, value)
	}
}
