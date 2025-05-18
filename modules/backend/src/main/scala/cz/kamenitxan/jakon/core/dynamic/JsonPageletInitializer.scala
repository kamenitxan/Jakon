package cz.kamenitxan.jakon.core.dynamic

import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.PageletInitializer.{MethodArgs, createMethodArgs}
import cz.kamenitxan.jakon.core.dynamic.arguments.ParsedValue
import cz.kamenitxan.jakon.core.dynamic.entity.*
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.I18nUtil
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.entity.Message
import io.javalin.http.{Context, Handler}

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.{Field, Method}
import java.sql.Connection
import scala.collection.immutable.Map
import scala.jdk.CollectionConverters.*

/**
 * Created by TPa on 13.04.2020.
 */
object JsonPageletInitializer {
	private val JsonContentType = "application/json"

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		Logger.info("Initializing json pagelets")
		controllers.foreach(c => {
			Logger.debug("Initializing json pagelet: " + c.getSimpleName)
			val controllerAnn = c.getAnnotation(classOf[JsonPagelet])
			if (controllerAnn.path().endsWith("/")) {
				Logger.warn(s"${c.getSimpleName} path ends with /. This is not recommended.")
			}

			c.getDeclaredMethods
				.filter(m => m.getAnnotation(classOf[Get]) != null || m.getAnnotation(classOf[Post]) != null)
				.foreach(m => {
					val get = m.getAnnotation(classOf[Get])
					val post = m.getAnnotation(classOf[Post])
					if (get != null) {
						initGetAnnotation(get, controllerAnn, m, c)
					}
					if (post != null) {
						initPostAnnotation(post, controllerAnn, m, c)
					}
					if ((get != null && get.path().endsWith("/")) || (post != null && post.path().endsWith("/"))) {
						Logger.warn(s"${c.getSimpleName}.${m.getName} path ends with /. This is not recommended.")
					}
					if ((get != null && get.path().nonEmpty && !get.path().startsWith("/")) || (post != null && post.path().nonEmpty && !post.path().startsWith("/"))) {
						Logger.warn(s"${c.getSimpleName}.${m.getName} path does not start with /. This is not recommended.")
					}
				})
		})

		Logger.info("Json pagelet initialization complete")
	}

	private def initGetAnnotation(get: Get, controllerAnn: JsonPagelet, m: Method, c: Class[_]): Unit = {
		JakonInit.javalin.get(controllerAnn.path() + get.path(), new Handler {
			override def handle(ctx: Context): Unit = {
				ctx.contentType(JsonContentType)
				val pagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[AbstractJsonPagelet]
				val response: String = DBHelper.withDbConnection(implicit conn => {
					val methodArgs = createMethodArgs(m, ctx, pagelet)
					try {
						val responseData = m.invoke(pagelet, methodArgs.array: _*)
						createResponse(responseData, pagelet)
					} catch {
						case ex: Exception =>
							Logger.error(s"${pagelet.getClass.getCanonicalName}.${m.getName}() threw exception", ex)
							createErrorResponse(ex, ctx, pagelet)
					} finally {
						methodArgs.connection.foreach(_.close())
					}
				})
				ctx.result(response)
			}
		})
	}

	private def initPostAnnotation(post: Post, controllerAnn: JsonPagelet, m: Method, c: Class[_]): Unit = {
		JakonInit.javalin.post(controllerAnn.path() + post.path(), new Handler {
			override def handle(ctx: Context): Unit = {
				ctx.contentType(JsonContentType)
				val controller = c.getDeclaredConstructor().newInstance().asInstanceOf[AbstractJsonPagelet]
				var methodArgs: PageletInitializer.MethodArgs = null
				try {
					val dataClass = PageletInitializer.getDataClass(m)
					val response = if (post.validate() && dataClass.isDefined) {
						val jsonData = controller.parseRequestData(ctx, dataClass.get)
						EntityValidator.validateJson(dataClass.get.getSimpleName, jsonData) match {
							case Left(result) =>
								val translatedErrors = result.map(m => {
									val ut = I18nUtil.getTranslation(Settings.getTemplateDir, "validations", m.text, Settings.getDefaultLocale)
									val t = if (ut == m.text) {
										I18nUtil.getTranslation("templates/admin", "validations", m.text, Settings.getDefaultLocale)
									} else {
										ut
									}
									new Message(m._severity, t, m.params, m.bundle)
								})
								createFailResponse(ctx, controller, translatedErrors)
							case Right(validatedData) =>
								methodArgs = parseJsonArgs(m, ctx, controller, validatedData)
								val methodArgsArray = methodArgs.array
								val responseData = m.invoke(controller, methodArgsArray: _*)
								createResponse(responseData, controller)
						}
					} else {
						methodArgs = parseJsonArgs(m, ctx, controller)
						val methodArgsArray = methodArgs.array
						val responseData = m.invoke(controller, methodArgsArray: _*)
						createResponse(responseData, controller)
					}
					ctx.result(response)
				} catch {
					case ex: Exception =>
						Logger.error(s"${controller.getClass.getCanonicalName}.${m.getName}() threw exception", ex)
						val response = createErrorResponse(ex, ctx, controller)
						ctx.result(response)
				} finally {
					if (methodArgs != null) {
						methodArgs.connection.foreach(_.close())
					}
				}
			}
		})
	}

	private def createResponse(responseData: AnyRef, controller: AbstractJsonPagelet): String = {
		if (responseData == null) {
			return ""
		}
		val wrap = (data: AnyRef) => {
			val jr = new AbstractJsonResponse(ResponseStatus.success, data) {}
			controller.encodeToJson(jr)
		}
		responseData match {
			case rd: AbstractJsonResponse[_] => controller.encodeToJson(rd)
			case rd: ApiResponse[_] => controller.encodeToJson(rd)
			case rd: String => if (controller.wrapResponse) {
				wrap(rd)
			} else {
				rd
			}
			case rd => if (controller.wrapResponse) {
				wrap(rd)
			} else {
				controller.encodeToJson(rd)
			}
		}
	}

	private def createFailResponse(ctx: Context, controller: AbstractJsonPagelet, messages: Seq[Message]): String = {
		ctx.status(400)
		val responseData = new JsonFailResponse(messages.asJava)
		controller.encodeToJson(responseData)
	}

	private def createErrorResponse(ex: Exception, ctx: Context, controller: AbstractJsonPagelet): String = {
		val msg = if (Settings.getDeployMode != DeployMode.PRODUCTION) {
			val sw = new StringWriter
			val pw = new PrintWriter(sw)
			ex.printStackTrace(pw)
			sw.toString
		} else {
			null
		}
		ctx.status(500)
		val responseData = new JsonErrorResponse(null, 1, msg)
		controller.encodeToJson(responseData)
	}

	private def parseJsonArgs(m: Method,
														ctx: Context,
														controller: AbstractJsonPagelet,
														validatedData: Map[Field, ParsedValue] = Map.empty
													 ): PageletInitializer.MethodArgs = {
		var dataRef: Any = null
		var conn: Connection = null
		val arr: Array[Any] = m.getParameterTypes.map {
			case CONTEXT_CLS => ctx
			case CONNECTION_CLS => DBHelper.getConnection
				conn = DBHelper.getConnection
				conn
			case t =>
				val data = controller.createDataObject(validatedData, t)
				dataRef = data
				Logger.debug(data.toString)
				data
		}
		new MethodArgs(arr, dataRef, Option.apply(conn))
	}
}
