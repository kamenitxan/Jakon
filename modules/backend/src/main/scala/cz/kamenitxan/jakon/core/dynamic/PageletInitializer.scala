package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.conform.FieldConformer.*
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import cz.kamenitxan.jakon.webui.{AdminSettings, Context}
import jakarta.servlet.MultipartConfigElement
import spark.{Request, Response, Spark}

import java.lang.reflect.Method
import java.sql.Connection
import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters.*


object PageletInitializer {
	private val METHOD_VALDIATE = "validate"
	private val gson = new Gson

	val protectedPrefixes: mutable.Buffer[String] = mutable.Buffer[String]()

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		Logger.info("Initializing pagelets")
		controllers.foreach(c => {
			Logger.debug("Initializing pagelet: " + c.getSimpleName)
			val controllerAnn = c.getAnnotation(classOf[Pagelet])
			if (controllerAnn.path().endsWith("/")) {
				Logger.warn(s"${c.getSimpleName} path ends with /. This is not recommended.")
			}
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
					if ((get != null && get.path().endsWith("/")) || (post != null && post.path().endsWith("/"))) {
						Logger.warn(s"${c.getSimpleName}.${m.getName} path ends with /. This is not recommended.")
					}
					if ((get != null && get.path().nonEmpty && !get.path().startsWith("/")) || (post != null && post.path().nonEmpty && !post.path().startsWith("/"))) {
						Logger.warn(s"${c.getSimpleName}.${m.getName} path does not start with /. This is not recommended.")
					}
				})
		})
		controllers.filter(c => classOf[AbstractAdminPagelet].isAssignableFrom(c) && c.getAnnotation(classOf[Pagelet]).showInAdmin()).foreach(c => {
			val apa = c.getDeclaredMethods.filter(m => m.getAnnotation(classOf[Get]) != null)
				.map(_.getAnnotation(classOf[Get]))
				.sortBy(_.path()).headOption
			if (apa.nonEmpty) {
				val inst = c.getDeclaredConstructor().newInstance().asInstanceOf[AbstractAdminPagelet]
				val controllerAnn = c.getAnnotation(classOf[Pagelet])
				val get = apa.get
				AdminSettings.customControllersInfo += new CustomControllerInfo(inst.name, inst.icon, controllerAnn.path() + get.path(), c)
			}
		})
		Logger.info("Pagelet initialization complete")
	}


	private def initGetAnnotation(get: Get, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		//TODO m.getReturnType.is
		Spark.get(controllerAnn.path() + get.path(), (req: Request, res: Response) => {
			val pagelet: IPagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]
			// TODO: vytvoreni conn pouze pokud je potreba
			DBHelper.withDbConnection(conn => {
				val methodArgs = createMethodArgs(m, req, res, conn, pagelet)
				var context = m.invoke(pagelet, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
				if (notRedirected(res)) {
					if (pagelet.isInstanceOf[AbstractAdminPagelet]) {
						if (context == null) {
							context = mutable.Map[String, Any]()
						}
						context = context ++ Context.getAdminContext
					}
					try {
						pagelet.render(context, get.template(), req)
					} catch {
						case ex: Exception =>
							Logger.error(s"${pagelet.getClass.getCanonicalName}.${m.getName}() threw exception", ex)
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
			val pagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]

			// TODO: vytvoreni conn pouze pokud je potreba
			DBHelper.withDbConnection(conn => {
				val dataClass = getDataClass(m)
				if (req.raw().getContentType.startsWith("multipart/form-data")) {
					req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"))
				}
				if (post.validate() && dataClass.isDefined) {
					val formData = EntityValidator.createFormData(req, dataClass.get)
					EntityValidator.validate(dataClass.get.getSimpleName, formData) match {
						case Left(result) =>
							if ("true".equals(req.queryParams(METHOD_VALDIATE))) {
								gson.toJson(result)
							} else {
								result.foreach(r => PageContext.getInstance().messages += r)
								val rp = formData.map(kv => (kv._1.getName, kv._2))

								val path = replacePathParams(controllerAnn.path() + post.path(), req.params.asScala)
								pagelet.redirect(req, res, path, rp)
							}
						case Right(_) =>
							if ("true".equals(req.queryParams(METHOD_VALDIATE))) {
								gson.toJson(true)
							} else {
								val methodArgs = createMethodArgs(m, req, res, conn, pagelet)
								invokePost(req, res, pagelet, m, post, methodArgs)
							}
					}
				} else {
					val methodArgs = createMethodArgs(m, req, res, conn, pagelet)
					invokePost(req, res, pagelet, m, post, methodArgs)
				}
			})
		})
	}

	@tailrec
	private def replacePathParams(path: String, params: mutable.Map[String, String]): String = {
		if (params.isEmpty) {
			path
		} else {
			val head = params.head
			val replaced = path.replace(head._1, head._2)
			replacePathParams(replaced, params.tail)
		}
	}

	private def invokePost(req: Request, res: Response, controller: IPagelet, m: Method, post: Post, methodArgs: MethodArgs) = {
		if (notRedirected(res)) {
			m.getReturnType match {
				case STRING =>
					m.invoke(controller, methodArgs.array: _*)
				case _ =>
					try {
						val context = m.invoke(controller, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
						if (notRedirected(res)) {
							controller.render(context, post.template(), req)
						} else {
							""
						}
					} catch {
						case ex: Exception =>
							Logger.error(s"${controller.getClass.getCanonicalName}.${m.getName}() threw exception", ex)
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

	private[dynamic] def createMethodArgs(m: Method, req: Request, res: Response, conn: Connection, pagelet: AnyRef): MethodArgs = {
		var dataRef: Any = null
		val arr = m.getParameterTypes.map {
			case REQUEST_CLS => req
			case RESPONSE_CLS => res
			case CONNECTION_CLS => conn
			case t =>
				val enclosingCls = t.getEnclosingClass
				val constructor = if (enclosingCls != null) t.getDeclaredConstructor(enclosingCls) else t.getDeclaredConstructor()
				val data = (if (enclosingCls != null) constructor.newInstance(pagelet) else constructor.newInstance()).asInstanceOf[AnyRef]
				Logger.debug(s"Creating pagelet data: {${t.getSimpleName}}")
				t.getDeclaredFields.foreach(f => {
					try {
						if (req.queryMap(f.getName).hasValue) {
							val value = req.queryMap(f.getName).values().mkString("\r\n")
							f.setAccessible(true)
							f.set(data, value.conform(f))
						}
					} catch {
						case ex: Exception => Logger.error("Exception when setting pagelet data value", ex)
					}
				})
				dataRef = data
				data
		}.asInstanceOf[Array[Any]]
		new MethodArgs(arr, dataRef)
	}

	class MethodArgs(val array: Array[Any], val data: Any)

}
