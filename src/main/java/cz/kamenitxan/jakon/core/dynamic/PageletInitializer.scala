package cz.kamenitxan.jakon.core.dynamic

import java.lang.reflect.Method
import java.sql.Connection

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.utils.i18nUtil
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.controler.pagelets.AbstractAdminPagelet
import cz.kamenitxan.jakon.webui.entity.CustomControllerInfo
import cz.kamenitxan.jakon.webui.{AdminSettings, Context}
import javax.validation.Validation
import org.slf4j.LoggerFactory
import spark.{Request, Response, Spark}

import scala.collection.JavaConverters._
import scala.collection.mutable


object PageletInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass)
	private val METHOD_VALDIATE = "validate"

	val protectedPrefixes = mutable.Buffer[String]()

	def initControllers(controllers: Seq[Class[_]]): Unit = {
		logger.info("Initializing pagelets")
		controllers.foreach(c => {
			logger.debug("Initializing pagelet: " + c.getSimpleName)
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
				val inst = c.newInstance().asInstanceOf[AbstractAdminPagelet]
				val controllerAnn = c.getAnnotation(classOf[Pagelet])
				val get = apa.get.getAnnotation(classOf[Get])
				AdminSettings.customControllersInfo += new CustomControllerInfo(inst.name, inst.icon, controllerAnn.path() + get.path())
			}
		})
		logger.info("Pagelet initialization complete")
	}


	private def initGetAnnotation(get: Get, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		//TODO m.getReturnType.is
		Spark.get(controllerAnn.path() + get.path(), (req, res) => {
			val controller: AbstractPagelet = c.newInstance().asInstanceOf[AbstractPagelet]
			withDbConnection(conn => {
				val methodArgs = createMethodArgs(m, req, res, conn)
				var context = m.invoke(controller, methodArgs.array:_*).asInstanceOf[mutable.Map[String, Any]]
				if (controller.isInstanceOf[AbstractAdminPagelet]) {
					if (context == null) {
						context = mutable.Map[String, Any]()
					}
					context = context ++ Context.getAdminContext
				}
				controller.render(context, get.template())
			})
		})
	}

	private def initPostAnnotation(post: Post, controllerAnn: Pagelet, m: Method, c: Class[_]): Unit = {
		Spark.post(controllerAnn.path() + post.path(), (req, res) => {
			val controller = c.newInstance().asInstanceOf[AbstractPagelet]

			withDbConnection(conn => {
				val methodArgs = createMethodArgs(m, req, res, conn)
				if (methodArgs.data != null && "true".equals(req.queryParams(METHOD_VALDIATE))) {
					// TODO share factory?
					val factory = Validation.buildDefaultValidatorFactory
					val validator = factory.getValidator
					val violations = validator.validate(methodArgs.data).asScala
					val gson = new Gson
					val result = violations.map(v => {
						val message = i18nUtil.getTranslation(c.getSimpleName, v.getMessage, Settings.getDefaultLocale)
						new ValidationResult(v.getPropertyPath.toString, message)
					}).toList.asJava
					gson.toJson(result)
				} else {
					val context = m.invoke(controller, methodArgs.array:_*).asInstanceOf[mutable.Map[String, Any]]
					controller.render(context, post.template())
				}
			})
		})
	}

	private def withDbConnection[T](fun: Connection => T): T = {
		val conn = DBHelper.getConnection
		try {
			fun.apply(conn)
		} finally {
			conn.close()
		}
	}

	private val REQUEST_CLS = classOf[Request]
	private val RESPONSE_CLS = classOf[Response]
	private val CONNECTION_CLS = classOf[Connection]
	private def createMethodArgs(m: Method, req: Request, res: Response, conn: Connection): MethodArgs = {
		var dataRef: AnyRef = null
		val arr = m.getParameterTypes.map {
			case REQUEST_CLS => req
			case RESPONSE_CLS => res
			case CONNECTION_CLS => conn
			case t =>
				val data = t.newInstance().asInstanceOf[AnyRef]
				logger.trace(s"Creating pagelet data: {${t.getSimpleName}}")
				t.getDeclaredFields.foreach(f => {
					try {
						f.setAccessible(true)
						f.set(data, req.queryParams(f.getName).conform(f))
					} catch {
						case ex: Exception => logger.error("Exception whem setting pagelet data value", ex)
					}
				})
				dataRef = data
				logger.trace(data.toString)
				data
		}
		new MethodArgs(arr, dataRef)
	}

	class MethodArgs(val array: Array[AnyRef], val data: AnyRef)
}