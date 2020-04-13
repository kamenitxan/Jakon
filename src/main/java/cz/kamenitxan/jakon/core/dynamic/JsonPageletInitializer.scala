package cz.kamenitxan.jakon.core.dynamic

import java.io.{PrintWriter, StringWriter}
import java.lang.reflect.Method

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.dynamic.PageletInitializer.createMethodArgs
import cz.kamenitxan.jakon.core.dynamic.entity.{AbstractJsonResponse, JsonErrorResponse, ResponseStatus}
import cz.kamenitxan.jakon.logging.Logger
import spark.{Request, Response, Spark}

import scala.collection.mutable

/**
 * Created by TPa on 13.04.2020.
 */
object JsonPageletInitializer {
	private val JsonContentType = "application/json";

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		Logger.info("Initializing json pagelets")
		controllers.foreach(c => {
			Logger.debug("Initializing json pagelet: " + c.getSimpleName)
			val controllerAnn = c.getAnnotation(classOf[JsonPagelet])

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
			  })
		})

		Logger.info("Json pagelet initialization complete")
	}

	private def initGetAnnotation(get: Get, controllerAnn: JsonPagelet, m: Method, c: Class[_]): Unit = {
		Spark.get(controllerAnn.path() + get.path(), (req: Request, res: Response) => {
			res.raw().setContentType(JsonContentType)
			val controller = c.getDeclaredConstructor().newInstance().asInstanceOf[AbstractJsonPagelet]
			DBHelper.withDbConnection(implicit conn => {
				val methodArgs = createMethodArgs(m, req, res, conn)
				try {
					val responseData = m.invoke(controller, methodArgs.array: _*)
					responseData match {
						case rd: AbstractJsonResponse => controller.gson.toJson(rd)
						case rd =>
							val jr = new AbstractJsonResponse(ResponseStatus.success, rd) {}
							controller.gson.toJson(jr, classOf[AbstractJsonResponse])
					}

				} catch {
					case ex: Exception =>
						Logger.error("Json pagelet get method threw exception", ex)
						val msg = if (Settings.getDeployMode != DeployMode.PRODUCTION) {
							val sw = new StringWriter
							val pw = new PrintWriter(sw)
							ex.printStackTrace(pw)
							sw.toString
						} else {
							null
						}
						res.status(500)
						val responseData = new JsonErrorResponse(null, 1, msg)
						controller.gson.toJson(responseData)
				}

			})
		})
	}

	private def initPostAnnotation(post: Post, controllerAnn: JsonPagelet, m: Method, c: Class[_]): Unit = {
		Spark.post(controllerAnn.path() + post.path(), (req: Request, res: Response) => {
			""
		})
	}
}
