package jakontest.utils

import io.javalin.config.Key
import io.javalin.http.{Context, HandlerType, HttpStatus}
import io.javalin.json.JsonMapper
import io.javalin.plugin.ContextPlugin
import io.javalin.security.RouteRole
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import jakontest.test.TestHttpServletRequest

import java.io.InputStream
import java.util
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

/**
 * Created by Kamenitxan on 15.11.2024
 */
class ScalalinServletContext extends Context {

	val testReq = {
		val req = new TestHttpServletRequest
		req.getSession(true)
		req
	}

	override def req(): HttpServletRequest = testReq

	override def res(): HttpServletResponse = ???

	override def appData[T](key: Key[T]): T = ???

	override def endpointHandlerPath(): String = ???

	override def future(supplier: Supplier[_ <: CompletableFuture[_]]): Unit = ???

	override def handlerType(): HandlerType = ???

	override def jsonMapper(): JsonMapper = ???

	override def matchedPath(): String = ???

	override def minSizeForCompression(i: Int): Context = ???

	override def outputStream(): ServletOutputStream = ???

	override def pathParam(s: String): String = ???

	override def pathParamMap(): util.Map[String, String] = ???

	override def redirect(s: String, httpStatus: HttpStatus): Unit = ???

	override def result(inputStream: InputStream): Context = ???

	override def resultInputStream(): InputStream = ???

	override def routeRoles(): util.Set[RouteRole] = ???

	override def skipRemainingHandlers(): Context = ???

	override def strictContentTypes(): Boolean = ???

	override def `with`[T](aClass: Class[_ <: ContextPlugin[_, T]]): T = ???

	override def writeJsonStream(stream: java.util.stream.Stream[_]): Unit = ???
}
