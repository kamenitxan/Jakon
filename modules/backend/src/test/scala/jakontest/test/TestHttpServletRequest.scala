package jakontest.test

import jakarta.servlet.*
import jakarta.servlet.http.*

import java.io.BufferedReader
import java.security.Principal
import java.util
import java.util.{Collections, Locale}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*


class TestHttpServletRequest extends HttpServletRequest {
	var session: HttpSession = _

	override def getAuthType: String = ???

	override def getCookies: Array[Cookie] = ???

	override def getDateHeader(name: String): Long = ???

	override def getHeader(name: String): String = ???

	override def getHeaders(name: String): util.Enumeration[String] = ???

	override def getHeaderNames: util.Enumeration[String] = ???

	override def getIntHeader(name: String): Int = ???

	override def getMethod: String = ???

	override def getPathInfo: String = "testPath"

	override def getPathTranslated: String = ???

	override def getContextPath: String = ???

	override def getQueryString: String = ???

	override def getRemoteUser: String = ???

	override def isUserInRole(role: String): Boolean = ???

	override def getUserPrincipal: Principal = ???

	override def getRequestedSessionId: String = ???

	override def getRequestURI: String = "testRequestURI"

	override def getRequestURL: StringBuffer = ???

	override def getServletPath: String = ???

	override def getSession(create: Boolean): HttpSession = {
		session = new HttpSession {
			private val attributes: mutable.Map[String, Any] = mutable.Map[String, Any]()

			override def getCreationTime: Long = ???

			override def getId: String = ???

			override def getLastAccessedTime: Long = ???

			override def getServletContext: ServletContext = ???

			override def setMaxInactiveInterval(interval: Int): Unit = ???

			override def getMaxInactiveInterval: Int = ???

			override def getSessionContext: HttpSessionContext = ???

			override def getAttribute(name: String): AnyRef = attributes.get(name).orNull.asInstanceOf[AnyRef]

			override def getValue(name: String): AnyRef = ???

			override def getAttributeNames: util.Enumeration[String] = {
				Collections.enumeration(attributes.keySet.asJava)
			}

			override def getValueNames: Array[String] = ???

			override def setAttribute(name: String, value: Any): Unit = attributes.put(name, value)

			override def putValue(name: String, value: Any): Unit = ???

			override def removeAttribute(name: String): Unit = ???

			override def removeValue(name: String): Unit = ???

			override def invalidate(): Unit = ???

			override def isNew: Boolean = ???
		}
		session
	}

	override def getSession: HttpSession = {
		if (session == null) {
			getSession(true)
		} else {
			session
		}
	}

	override def changeSessionId(): String = ???

	override def isRequestedSessionIdValid: Boolean = ???

	override def isRequestedSessionIdFromCookie: Boolean = ???

	override def isRequestedSessionIdFromURL: Boolean = ???

	override def isRequestedSessionIdFromUrl: Boolean = ???

	override def authenticate(response: HttpServletResponse): Boolean = ???

	override def login(username: String, password: String): Unit = ???

	override def logout(): Unit = ???

	override def getParts: util.Collection[Part] = ???

	override def getPart(name: String): Part = ???

	override def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T = ???

	override def getAttribute(name: String): AnyRef = ???

	override def getAttributeNames: util.Enumeration[String] = ???

	override def getCharacterEncoding: String = ???

	override def setCharacterEncoding(env: String): Unit = ???

	override def getContentLength: Int = ???

	override def getContentLengthLong: Long = ???

	override def getContentType: String = ???

	override def getInputStream: ServletInputStream = ???

	override def getParameter(name: String): String = ???

	override def getParameterNames: util.Enumeration[String] = ???

	override def getParameterValues(name: String): Array[String] = ???

	override def getParameterMap: util.Map[String, Array[String]] = ???

	override def getProtocol: String = ???

	override def getScheme: String = ???

	override def getServerName: String = ???

	override def getServerPort: Int = ???

	override def getReader: BufferedReader = ???

	override def getRemoteAddr: String = ???

	override def getRemoteHost: String = ???

	override def setAttribute(name: String, o: Any): Unit = ???

	override def removeAttribute(name: String): Unit = ???

	override def getLocale: Locale = ???

	override def getLocales: util.Enumeration[Locale] = ???

	override def isSecure: Boolean = ???

	override def getRequestDispatcher(path: String): RequestDispatcher = ???

	override def getRealPath(path: String): String = ???

	override def getRemotePort: Int = ???

	override def getLocalName: String = ???

	override def getLocalAddr: String = ???

	override def getLocalPort: Int = ???

	override def getServletContext: ServletContext = ???

	override def startAsync(): AsyncContext = ???

	override def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse): AsyncContext = ???

	override def isAsyncStarted: Boolean = ???

	override def isAsyncSupported: Boolean = ???

	override def getAsyncContext: AsyncContext = ???

	override def getDispatcherType: DispatcherType = ???
}
