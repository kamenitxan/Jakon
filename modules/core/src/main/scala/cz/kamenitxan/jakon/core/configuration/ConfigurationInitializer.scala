package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.utils.Utils
import org.slf4j.LoggerFactory

import java.io.{File, FileInputStream, IOException}
import java.util.Properties
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps
import scala.util.Using


object ConfigurationInitializer {
	private val logger = LoggerFactory.getLogger(this.getClass.getName)
	private val conf = mutable.HashMap[String, String]()

	/**
	 * Optional file with secrets (API tokens, passwords, ...) that must not be committed with the main config.
	 * It is looked up next to the main config file and its values override the main config ones.
	 */
	val SecretsFileName = "jakon_secrets.properties"


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
				load(configFile)
				val secretsFile = new File(configFile.getAbsoluteFile.getParentFile, SecretsFileName)
				if (secretsFile.isFile) {
					val keys = load(secretsFile)
				}
			}
		}
	}

	/** Puts all properties of the file into the configuration, overriding existing keys; returns the loaded keys. */
	private def load(file: File): Seq[String] = {
		val prop = new Properties
		Using.resource(new FileInputStream(file))(input => prop.load(input))
		val keys = prop.stringPropertyNames.asScala.toSeq
		keys.foreach(key => conf.put(key, prop.getProperty(key).trim))
		keys
	}

	def getConf: mutable.Map[String, String] = conf


	def initConfiguration(configClasses: Seq[Class[?]]): Unit = {
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
				if (setter.nonEmpty && confValue.nonEmpty) {
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
