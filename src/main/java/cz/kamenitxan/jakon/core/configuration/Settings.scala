package cz.kamenitxan.jakon.core.configuration

import java.io.{File, FileInputStream, IOException}
import java.util._

import cz.kamenitxan.jakon.core.template.{FixedPebbleTemplateEngine, Pebble, TemplateEngine}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.mail.EmailTypeHandler
import cz.kamenitxan.jakon.webui.util.JakonFileLoader
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Settings {
	private val logger = LoggerFactory.getLogger(this.getClass.getName)
	private var engine: TemplateEngine = _
	private var adminEngine: spark.TemplateEngine = _
	private val settings = mutable.Map[SettingValue, String]()
	private var emailTypeHandler: EmailTypeHandler = _

	init(new File("jakon_config.properties"))

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
		}
		val input = new FileInputStream(configFile)
		val prop = new Properties
		prop.load(input)
		val e = prop.propertyNames
		while ( {
			e.hasMoreElements
		}) {
			val key = e.nextElement.asInstanceOf[String]
			val value = prop.getProperty(key).trim
			try settings.put(SettingValue.fromName(key), value)
			catch {
				case ignored: IllegalArgumentException =>
					logger.error("Cant load setting value", e)
			}
		}
		if (getStaticDir == getOutputDir) throw new IllegalArgumentException("Static and output directory must not be same")
		val loader = new JakonFileLoader
		loader.setSuffix(".peb")
		adminEngine = new FixedPebbleTemplateEngine(loader)
		setTemplateEngine(new Pebble)
	}

	def getTemplateDir: String = settings.get(SettingValue.TEMPLATE_DIR).orNull

	def setTemplateDir(templateDir: String): Unit = settings.put(SettingValue.TEMPLATE_DIR, templateDir)

	def getTemplateEngine: TemplateEngine = engine

	def setTemplateEngine(engine: TemplateEngine): Unit = this.engine = engine

	def getAdminEngine: spark.TemplateEngine = adminEngine

	def setAdminEngine(adminEngine: spark.TemplateEngine): Unit = this.adminEngine = adminEngine

	def getStaticDir: String = settings.get(SettingValue.STATIC_DIR).orNull

	def setStaticDir(staticDir: String): Unit = settings.put(SettingValue.STATIC_DIR, staticDir)

	def getOutputDir: String = settings.get(SettingValue.OUTPUT_DIR).orNull

	def setOutputDir(outputDir: String): Unit = settings.put(SettingValue.OUTPUT_DIR, outputDir)

	def getPackage: Array[String] = settings.getOrElse(SettingValue.PACKAGE, "").split(";")

	def getDatabaseDriver: String = settings.get(SettingValue.DB_DRIVER).orNull

	def setDatabaseDriver(databaseDriver: String): Unit = settings.put(SettingValue.DB_DRIVER, databaseDriver)

	def getDatabaseConnPath: String = settings.get(SettingValue.DB_URL).orNull

	def setDatabaseConnPath(databaseConnPath: String): Unit = settings.put(SettingValue.DB_URL, databaseConnPath)

	def getPort: Int = Integer.valueOf(settings.get(SettingValue.PORT).orNull)

	def setPort(port: Int): Unit = settings.put(SettingValue.PORT, String.valueOf(port))

	def getDefaultLocale: Locale = Utils.stringToLocale(settings.get(SettingValue.DEFAULT_LOCALE).orNull)

	def getProperty(name: SettingValue): String = {
		val prop = settings.get(name)
		if (prop.isEmpty && name.defaultValue != null) {
			return name.defaultValue
		}
		prop.orNull
	}

	def getDeployMode: DeployMode = {
		val mode = settings.get(SettingValue.DEPLOY_MODE)
		if (mode != null) DeployMode.valueOf(mode.orNull)
		else DeployMode.PRODUCTION
	}

	def setDeployMode(mode: DeployMode): Unit = settings.put(SettingValue.DEPLOY_MODE, mode.name)

	def getEmailTypeHandler: EmailTypeHandler = emailTypeHandler

	def setEmailTypeHandler(handler: EmailTypeHandler): Unit = emailTypeHandler = handler

	def isEmailEnabled: Boolean = {
		getProperty(SettingValue.MAIL_ENABLED).toBoolean
	}
}