package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.utils.TypeReferences._
import cz.kamenitxan.jakon.utils.Utils
import org.slf4j.LoggerFactory

import java.io.{File, FileInputStream, IOException}
import java.util.Properties
import scala.collection.mutable
import scala.language.postfixOps


object ConfigurationInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass.getName)
	private val conf = mutable.HashMap[String, String]()


	@throws[IOException]
	def init(configFile: File): Unit = {
		Utils.measured(runtime => s"Configuration scanned in $runtime ms") {
			if (configFile == null) {
				try {
					init(new File("jakon_config.properties"))
				} catch {
					case e: Exception =>
						logger.error("Config loading failed. Shutting down!", e)
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
	}

	def getConf: mutable.Map[String, String] = conf


	def initConfiguration(configClasses: Seq[Class[_]]): Unit = {
		configClasses.foreach(c => {
			c.getDeclaredFields.filter(f => f.isAnnotationPresent(classOf[ConfigurationValue])).foreach(f => {
				val obj = c.getField("MODULE$").get(c)
				val ann = f.getAnnotation(classOf[ConfigurationValue])
				var confValue = conf.get(ann.name())
				if (confValue.isEmpty) {
					if (ann.defaultValue() != "") {
						confValue = Option(ann.defaultValue())
					} else if (ann.required()) {
						throw new IllegalStateException(ann.name + " is required")
					}
				}

				val setter = c.getDeclaredMethods
				  .filter(m => m.getParameterTypes.forall(cls => cls == classOf[String]))
				  .find(m => m.getName.equalsIgnoreCase("set" + f.getName))
				if (setter.nonEmpty) {
					if (setter.get.getParameterTypes.forall(cls => cls == classOf[String])) {
						setter.get.invoke(obj, confValue.get)
					} else {
						logger.warn("Ignoring setter: " + setter.get.getName)
					}
				} else if (confValue.nonEmpty) {
					f.setAccessible(true)
					f.getType match {
						case BOOLEAN => f.setBoolean(obj, confValue.get toBoolean)
						case DOUBLE => f.setDouble(obj, confValue.get toDouble)
						case INTEGER => f.setInt(obj, confValue.get toInt)
						case _ => f.set(obj, confValue.get)
					}
				}
			})
		})
		Settings.doAfterLoad()
	}
}
