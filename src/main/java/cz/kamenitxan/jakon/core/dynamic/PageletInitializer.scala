package cz.kamenitxan.jakon.core.dynamic

import java.lang.reflect.Method
import java.sql.Connection

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import cz.kamenitxan.jakon.webui.{AdminSettings, Context}
import spark.{Request, Response, Spark}

import scala.collection.mutable


object PageletInitializer {
	private val METHOD_VALDIATE = "validate"
	private val gson = new Gson

	val protectedPrefixes = mutable.Buffer[String]()

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		Logger.info("Initializing pagelets")
		controllers.foreach(c => {
			Logger.debug("Initializing pagelet: " + c.getSimpleName)
			val controllerAnn = c.getAnnotation(classOf[Pagelet])
			if (controllerAnn.authRequired()) {
				protectedPrefixes += controllerAnn.path()
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
			  })
		})
		controllers.filter(c => classOf[AbstractAdminPagelet].isAssignableFrom(c) && c.getAnnotation(classOf[Pagelet]).showInAdmin()).foreach(c => {
			val apa = c.getDeclaredMethods.find(m => m.getAnnotation(classOf[Get]) != null)
			if (apa.nonEmpty) {
				val inst = c.getDeclaredConstructor().newInstance().asInstanceOf[AbstractAdminPagelet]
				val controllerAnn = c.getAnnotation(classOf[Pagelet])
				val get = apa.get.getAnnotation(classOf[Get])
				AdminSettings.customControllersInfo += new CustomControllerInfo(inst.name, inst.icon, controllerAnn.path() + get.path(), c)
			}
		})
		Logger.info("Pagelet initialization complete")
	}


	private def initGetAnnotation(get: Get, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		//TODO m.getReturnType.is
		Spark.get(controllerAnn.path() + get.path(), (req: Request, res: Response) => {
			val controller: IPagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]
			DBHelper.withDbConnection(conn => {
				val methodArgs = createMethodArgs(m, req, res, conn)
				var context = m.invoke(controller, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
				if (notRedirected(res)) {
					if (controller.isInstanceOf[AbstractAdminPagelet]) {
						if (context == null) {
							context = mutable.Map[String, Any]()
						}
						context = context ++ Context.getAdminContext
					}
					try {
						controller.render(context, get.template(), req)
					} catch {
						case ex: Exception =>
							Logger.error("Pagelet get method threw exception", ex)
							throw ex
					}
				} else {
					""
				}
			})
		})
	}

	private def initPostAnnotation(post: Post, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		Spark.post(controllerAnn.path() + post.path(), (req: Request, res: Response) => {
			val controller = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]

			DBHelper.withDbConnection(conn => {
				val dataClass = getDataClass(m)
				if (post.validate() && dataClass.isDefined) {
					val formData = EntityValidator.createFormData(req, dataClass.get)
					EntityValidator.validate(dataClass.get.getSimpleName, formData) match {
						case Left(result) =>
							if ("true".equals(req.queryParams(METHOD_VALDIATE))) {
								gson.toJson(result)
							} else {
								result.foreach(r => PageContext.getInstance().messages += r)
								val rp = formData.map(kv => (kv._1.getName, kv._2))
								controller.redirect(req, res, controllerAnn.path() + post.path(), rp)
							}
						case Right(_) =>
							if ("true".equals(req.queryParams(METHOD_VALDIATE))) {
								gson.toJson(true)
							} else {
								val methodArgs = createMethodArgs(m, req, res, conn)
								invokePost(req, res, controller, m, post, methodArgs)
							}
					}
				} else {
					val methodArgs = createMethodArgs(m, req, res, conn)
					invokePost(req, res, controller, m, post, methodArgs)
				}
			})
		})
	}

	private def invokePost(req: Request, res: Response, controller: IPagelet, m: Method, post: Post, methodArgs: MethodArgs) = {
		if (notRedirected(res)) {
			m.getReturnType match {
				case STRING =>
					m.invoke(controller, methodArgs.array: _*)
				case _ =>
					try {
						val context = m.invoke(controller, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
						controller.render(context, post.template(), req)
					} catch {
						case ex: Exception =>
							Logger.error("Pagelet post method threw exception", ex)
							throw ex
					}
			}
		} else {
			""
		}
	}

	private def notRedirected(res: Response) = {
		if (res.raw().getStatus == 302 || res.raw().getStatus == 301) {
			false
		} else {
			true
		}
	}


	def getDataClass(m: Method): Option[Class[_]] = {
		m.getParameterTypes.find(c => c != REQUEST_CLS && c != RESPONSE_CLS && c != CONNECTION_CLS)
	}

	private[dynamic] def createMethodArgs(m: Method, req: Request, res: Response, conn: Connection): MethodArgs = {
		var dataRef: Any = null
		val arr = m.getParameterTypes.map {
			case REQUEST_CLS => req
			case RESPONSE_CLS => res
			case CONNECTION_CLS => conn
			case t =>
				val data = t.getDeclaredConstructor().newInstance().asInstanceOf[AnyRef]
				Logger.debug(s"Creating pagelet data: {${t.getSimpleName}}")
				t.getDeclaredFields.foreach(f => {
					try {
						val value = req.queryMap(f.getName).values().mkString("\r\n")
						f.setAccessible(true)
						f.set(data, value.conform(f))
					} catch {
						case ex: Exception => Logger.error("Exception when setting pagelet data value", ex)
					}
				})
				dataRef = data
				Logger.debug(data.toString)
				data
		}.asInstanceOf[Array[Any]]
		new MethodArgs(arr, dataRef)
	}

	class MethodArgs(val array: Array[Any], val data: Any)

}
