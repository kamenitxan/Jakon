package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.Gson
import cz.kamenitxan.jakon.JakonInit
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.AdminSettings
import cz.kamenitxan.jakon.webui.conform.FieldConformer.*
import cz.kamenitxan.jakon.webui.controller.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import io.javalin.http.{Context, Handler}

import java.lang.reflect.Method
import java.sql.Connection
import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters.*


object PageletInitializer {
	private val METHOD_VALIDATE = "validate"
	private val gson = new Gson

	val protectedPrefixes: mutable.Buffer[String] = mutable.Buffer[String]()

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		Logger.info("Initializing pagelets")
		val (activeControllers, disabledControllers) = controllers.partition(PageletSettings.isEnabled)
		disabledControllers.foreach(c => Logger.info(s"Pagelet ${c.getSimpleName} is disabled by configuration"))
		activeControllers.foreach(c => {
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
				.filter(m => checkDataClass(c, m))
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
		activeControllers.filter(c => classOf[AbstractAdminPagelet].isAssignableFrom(c)
			&& c.getAnnotation(classOf[Pagelet]).showInAdmin()).foreach(c => {
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
		JakonInit.javalinConfig.routes.get(controllerAnn.path() + get.path(), new Handler {
			override def handle(ctx: Context): Unit = {
				val pagelet: IPagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]
				val methodArgs = createMethodArgs(m, ctx, pagelet)
				try {
					var context = m.invoke(pagelet, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
					if (ctx.result() == null) {
						if (notRedirected(ctx)) {
							if (pagelet.isInstanceOf[AbstractAdminPagelet]) {
								if (context == null) {
									context = mutable.Map[String, Any]()
								}
								context = context ++ cz.kamenitxan.jakon.webui.Context.getAdminContext ++ mutable.Map("pathInfo" -> ctx.path())
							}
							try {
								val res = pagelet.render(context, get.template(), ctx)
								ctx.result(res)
							} catch {
								case ex: Exception =>
									Logger.error(s"${pagelet.getClass.getCanonicalName}.${m.getName}() threw exception", ex)
									throw ex
							}
						} else {
							ctx.result("")
						}
					}
				} finally {
					methodArgs.connection.foreach(_.close())
				}
			}
		})
	}

	private def initPostAnnotation(post: Post, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		JakonInit.javalinConfig.routes.post(controllerAnn.path() + post.path(), new Handler {
			override def handle(ctx: Context): Unit = {
				val pagelet = c.getDeclaredConstructor().newInstance().asInstanceOf[IPagelet]

				val dataClass = getDataClass(m)
				if (post.validate() && dataClass.isDefined) {
					val formData = EntityValidator.createFormData(ctx, dataClass.get)
					EntityValidator.validate(dataClass.get.getSimpleName, formData) match {
						case Left(result) =>
							if ("true".equals(ctx.queryParam(METHOD_VALIDATE))) {
								val res = gson.toJson(result)
								ctx.result(res)
							} else {
								result.foreach(r => PageContext.getInstance().messages += r)
								val rp = formData.map(kv => (kv._1.getName, kv._2))

								val path = replacePathParams(controllerAnn.path() + post.path(), ctx.pathParamMap().asScala)
								pagelet.redirect(ctx, path, rp)
							}
						case Right(_) =>
							val result = if ("true".equals(ctx.queryParam(METHOD_VALIDATE))) {
								gson.toJson(true)
							} else {
								val methodArgs = createMethodArgs(m, ctx, pagelet)
								try {
									invokePost(ctx, pagelet, m, post, methodArgs)
								} finally {
									methodArgs.connection.foreach(_.close())
								}
							}
							ctx.result(result)
					}
				} else {
					val methodArgs = createMethodArgs(m, ctx, pagelet)
					try {
						val result = invokePost(ctx, pagelet, m, post, methodArgs)
						ctx.result(result)
					} finally {
						methodArgs.connection.foreach(_.close())
					}
				}
			}
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

	private def invokePost(ctx: Context, controller: IPagelet, m: Method, post: Post, methodArgs: MethodArgs): String = {
		if (notRedirected(ctx)) {
			m.getReturnType match {
				case STRING =>
					m.invoke(controller, methodArgs.array: _*).asInstanceOf[String]
				case _ =>
					try {
						val context = m.invoke(controller, methodArgs.array: _*).asInstanceOf[mutable.Map[String, Any]]
						if (notRedirected(ctx)) {
							controller.render(context, post.template(), ctx)

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

	private def notRedirected(ctx: Context) = {
		if (ctx.statusCode() == 302 || ctx.statusCode() == 301) {
			false
		} else {
			true
		}
	}


	def getDataClass(m: Method): Option[Class[_]] = {
		m.getParameterTypes.find(c => c != CONTEXT_CLS && c != CONNECTION_CLS)
	}

	private[dynamic] def createMethodArgs(m: Method, ctx: Context, pagelet: AnyRef): MethodArgs = {
		var dataRef: Any = null
		var conn: Connection = null
		val arr = m.getParameterTypes.map {
			case CONTEXT_CLS => ctx
			case CONNECTION_CLS => 
				conn = DBHelper.getConnection
				conn
			case t =>
				Logger.debug(s"Creating pagelet data: {${t.getSimpleName}}")
				val data = createDataObject(t, ctx, pagelet)
				dataRef = data
				data
		}.asInstanceOf[Array[Any]]
		new MethodArgs(arr, dataRef, Option.apply(conn))
	}

	/** Checks that pagelet data class is case class. Method with invalid data class is logged and should be ignored. */
	private[dynamic] def checkDataClass(c: Class[_], m: Method): Boolean = {
		getDataClass(m) match {
			case Some(dc) if !classOf[Product].isAssignableFrom(dc) =>
				Logger.error(s"${c.getSimpleName}.${m.getName}: pagelet data class ${dc.getSimpleName} must be a case class. Method is ignored.")
				false
			case _ => true
		}
	}

	/** Creates pagelet case class data object from form and query params */
	private def createDataObject(t: Class[_], ctx: Context, pagelet: AnyRef): AnyRef = {
		val values: Map[String, Any] = t.getDeclaredFields.flatMap(f => {
			try {
				val paramName = EntityValidator.getParamName(f)
				val value = if (ctx.formParam(paramName) != null && ctx.formParam(paramName).nonEmpty) {
					Option(ctx.formParam(paramName))
				} else if (!ctx.queryParams(paramName).isEmpty) {
					Option(ctx.queryParams(paramName).asScala.mkString("\r\n"))
				} else {
					None
				}
				value.map(v => f.getName -> v.conform(f))
			} catch {
				case ex: Exception =>
					Logger.error("Exception when setting pagelet data value", ex)
					None
			}
		}).toMap

		val enclosingCls = t.getEnclosingClass
		val constructor = t.getDeclaredConstructors.head
		val params = constructor.getParameters.zipWithIndex.map((p, i) => {
			if (i == 0 && enclosingCls != null && p.getType == enclosingCls) {
				pagelet
			} else {
				values.get(p.getName).orNull match {
					case null if p.getType.isPrimitive => defaultPrimitive(p.getType)
					case v => v
				}
			}
		})
		constructor.newInstance(params: _*).asInstanceOf[AnyRef]
	}

	private def defaultPrimitive(t: Class[_]): Any = t match {
		case java.lang.Boolean.TYPE => false
		case java.lang.Character.TYPE => 0.toChar
		case java.lang.Byte.TYPE => 0.toByte
		case java.lang.Short.TYPE => 0.toShort
		case java.lang.Integer.TYPE => 0
		case java.lang.Long.TYPE => 0L
		case java.lang.Float.TYPE => 0f
		case java.lang.Double.TYPE => 0d
		case _ => null
	}

	class MethodArgs(val array: Array[Any], val data: Any, val connection: Option[Connection])

}
