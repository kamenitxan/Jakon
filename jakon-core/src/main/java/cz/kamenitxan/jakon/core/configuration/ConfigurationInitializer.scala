package cz.kamenitxan.jakon.core.configuration

import java.io.{File, FileInputStream, IOException}
import java.util.Properties

import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.postfixOps
import scala.reflect.runtime.universe


object ConfigurationInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass.getName)
	private val conf = mutable.HashMap[String, String]()


	@throws[IOException]
	def init(configFile: File): Unit = {
		if (configFile == null) {
			try {
				init(new File("jakon_config.properties"))
			} catch {
				case e: Exception =>
					logger.error("Config loading failed. Shuting down!", e)
					System.exit(-1)
			}
		} else {
			val input = new FileInputStream(configFile)
			val prop = new Properties
			prop.load(input)
			val e = prop.propertyNames
			while ( {
				e.hasMoreElements
			}) {
				val key = e.nextElement.asInstanceOf[String]
				val value = prop.getProperty(key).trim
				conf.put(key, value)
			}
		}
	}

	def getConf: mutable.Map[String, String] = conf

	private val B = classOf[Boolean]
	private val D = classOf[Double]
	private val I = classOf[Int]
	def initConfiguration(configClasses: Seq[Class[_]]): Unit = {
		configClasses.foreach(c => {
			c.getDeclaredFields.filter(f => f.isAnnotationPresent(classOf[ConfigurationValue])).foreach(f => {
				val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
				val module = runtimeMirror.staticModule(c.getName)
				val obj = runtimeMirror.reflectModule(module).instance
				val ann = f.getAnnotation(classOf[ConfigurationValue])
				var confValue = conf.get(ann.name())
				if (ann.required() && confValue.isEmpty) {
					if (ann.defaultValue() != "") {
						confValue = Option(ann.defaultValue())
					} else if (ann.required()) {
						throw new IllegalStateException(ann.name + " is required")
					}
				}

				val setter = c.getDeclaredMethods.find(m => m.getName.equalsIgnoreCase("set" + f.getName))
				if (setter.nonEmpty) {
					if (setter.get.getParameterTypes.forall(cls => cls == classOf[String])) {
						setter.get.invoke(obj, confValue.get)
					} else {
						logger.warn("Ignoring setter: " + setter.get.getName)
					}
				} else if (confValue.nonEmpty) {
					f.setAccessible(true)
					f.getType match {
						case B => f.setBoolean(obj, confValue.get toBoolean)
						case D => f.setDouble(obj, confValue.get toDouble)
						case I => f.setInt(obj, confValue.get toInt)
						case _ => f.set(obj, confValue.get)
					}
				}
			})
		})
		Settings.doAfterLoad()
	}
}
