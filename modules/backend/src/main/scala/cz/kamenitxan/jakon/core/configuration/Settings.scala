package cz.kamenitxan.jakon.core.configuration

import cz.kamenitxan.jakon.core.template.utils.JakonPebbleTemplateEngine
import cz.kamenitxan.jakon.core.template.{Pebble, TemplateEngine}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.mail.EmailTypeHandler
import cz.kamenitxan.jakon.webui.util.JakonFileLoader
import jakarta.mail.Message

import java.util.*
import scala.language.postfixOps

/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
//noinspection VarCouldBeVal,ScalaUnusedSymbol
@Configuration
object Settings {
	private var engine: TemplateEngine = _
	private var adminEngine: spark.TemplateEngine = _
	private var emailTypeHandler: EmailTypeHandler = new EmailTypeHandler {
		override def handle(emailType: String): (Message, Predef.Map[String, Any]) => Unit = (_, _) => {}

		override def afterSend(emailType: String): Unit = {
			// override this to execute something after emails is send
		}
	}
	private var databaseType: DatabaseType = _

	def doAfterLoad(): Unit = {
		val loader = new JakonFileLoader("templates/admin", true)
		loader.setSuffix(".peb")
		adminEngine = new JakonPebbleTemplateEngine(loader)
		setTemplateEngine(new Pebble)
		getDatabaseConnPath match {
			case p if p.startsWith("jdbc:mysql:") => databaseType = DatabaseType.MYSQL
			case p if p.startsWith("jdbc:sqlite:") => databaseType = DatabaseType.SQLITE
		}
	}

	@ConfigurationValue(name = "templateDir", required = true, defaultValue = "templates/bacon")
	private var templateDir: String = _

	@ConfigurationValue(name = "staticDir", required = true, defaultValue = "static")
	private var staticDir: String = _

	@ConfigurationValue(name = "outputDir", required = true, defaultValue = "out/")
	private var outputDir: String = _

	private var pckg: Array[String] = _

	@ConfigurationValue(name = "databaseDriver", required = true, defaultValue = "org.sqlite.JDBC")
	private var databaseDriver: String = _

	@ConfigurationValue(name = "databaseConnPath", required = true, defaultValue = "jdbc:sqlite:jakonTest.sqlite")
	private var databaseConnPath: String = _

	@ConfigurationValue(name = "databaseUser", required = false, defaultValue = "")
	private var databaseUser: String = _

	@ConfigurationValue(name = "databasePass", required = false, defaultValue = "")
	private var databasePass: String = _

	@ConfigurationValue(name = "port", required = true, defaultValue = "4567")
	private var port: Int = _

	@ConfigurationValue(name = "defaultLocale", required = true, defaultValue = "en_US")
	private var defaultLocale: Locale = _

	@ConfigurationValue(name = "supportedLocales", required = false, defaultValue = "en_US")
	private var supportedLocales: Seq[Locale] = _

	@ConfigurationValue(name = "deployMode", required = true, defaultValue = "DEVEL")
	private var deployMode: DeployMode = _

	@ConfigurationValue(name = "deployType", required = true, defaultValue = "cz.kamenitxan.jakon.core.deploy.DummyDeploy")
	private var deployType: String = _

	@ConfigurationValue(name = "MAIL.enabled", required = true, defaultValue = "true")
	private var emailEnabled: Boolean = _

	@ConfigurationValue(name = "MAIL.auth", required = false)
	private var emailAuth: String = _

	@ConfigurationValue(name = "MAIL.tls", required = false, defaultValue = "false")
	private var emailTls: Boolean = _

	@ConfigurationValue(name = "MAIL.ssl", required = false, defaultValue = "false")
	private var emailSSL: Boolean = _

	@ConfigurationValue(name = "MAIL.host", required = false)
	private var emailHost: String = _

	@ConfigurationValue(name = "MAIL.port", required = false, defaultValue = "25")
	private var emailPort: String = _

	@ConfigurationValue(name = "MAIL.username", required = false)
	private var emailUserName: String = _

	@ConfigurationValue(name = "MAIL.password", required = false)
	private var emailPassword: String = _

	@ConfigurationValue(name = "MAIL.force_bcc", required = false)
	private var emailForceBcc: String = _

	@ConfigurationValue(name = "encryptionSecret", required = true, defaultValue = "reallyRandomStin")
	private var encryptionSecret: String = _

	@ConfigurationValue(name = "hostname", required = true)
	private var hostname: String = _

	@ConfigurationValue(name = "loginPath", required = true, defaultValue = "/login")
	private var loginPath: String = _

	@ConfigurationValue(name = "emailValidator.checkDns", required = false, defaultValue = "true")
	private var emailValidatorCheckDns: Boolean = _

	@ConfigurationValue(name = "HCAPTCHA.secret", required = false)
	private var hCaptchaSecret: String = _

	@ConfigurationValue(name = "HCAPTCHA.siteKey", required = false)
	private var hCaptchaSiteKey: String = _

	def getTemplateDir: String = templateDir

	def setTemplateDir(templateDir: String): Unit = this.templateDir = templateDir

	def getTemplateEngine: TemplateEngine = engine

	def setTemplateEngine(engine: TemplateEngine): Unit = this.engine = engine

	def getAdminEngine: spark.TemplateEngine = adminEngine

	def setAdminEngine(adminEngine: spark.TemplateEngine): Unit = this.adminEngine = adminEngine

	def getStaticDir: String = staticDir

	def setStaticDir(staticDir: String): Unit = {
		if (outputDir != null && staticDir == outputDir) {
			throw new IllegalArgumentException("Static and output directory must not be same")
		}
		this.staticDir = staticDir
	}

	def getOutputDir: String = outputDir

	def setOutputDir(outputDir: String): Unit = {
		if (staticDir != null && staticDir == outputDir) {
			throw new IllegalArgumentException("Static and output directory must not be same")
		}
		this.outputDir = outputDir
	}

	def getPackage: Array[String] = {
		if (pckg != null) return pckg
		pckg = ConfigurationInitializer.getConf("package").split(";")
		if (!pckg.contains("cz.kamenitxan.jakon")) {
			pckg = pckg :+ "cz.kamenitxan.jakon"
		}
		pckg
	}

	def getDatabaseDriver: String = databaseDriver

	def getDatabaseConnPath: String = databaseConnPath

	def getDatabaseUser: String = databaseUser

	def getDatabasePass: String = databasePass

	def getDatabaseType: DatabaseType = databaseType

	def getPort: Int = port

	def setPort(port: String): Unit = this.port = port toInt

	def getDefaultLocale: Locale = defaultLocale

	def setDefaultLocale(locale: String): Unit = defaultLocale = Utils.stringToLocale(locale)

	def getSupportedLocales: Seq[Locale] = supportedLocales

	def setSupportedLocales(locales: String): Unit = {
		supportedLocales = locales.split(",").map(_.trim).map(l => Utils.stringToLocale(l)).toSeq
	}

	def getDeployMode: DeployMode = deployMode

	def setDeployMode(deployMode: String): Unit = this.deployMode = {
		try {
			DeployMode.valueOf(deployMode)
		} catch {
			case ex: IllegalArgumentException =>
				Logger.error(s"Unknown deploy mode $deployMode. Setting PRODUCTION instead")
				DeployMode.PRODUCTION
		}
	}

	def setDeployMode(deployMode: DeployMode): Unit = this.deployMode = deployMode

	def getDeployType: String = deployType

	def getEmailTypeHandler: EmailTypeHandler = emailTypeHandler

	def setEmailTypeHandler(handler: EmailTypeHandler): Unit = emailTypeHandler = handler

	def isEmailEnabled: Boolean = emailEnabled

	def getEmailAuth: String = emailAuth

	def getEmailTls: Boolean = emailTls

	def getEmailSSL: Boolean = emailSSL

	def getEmailHost: String = emailHost

	def getEmailPort: String = emailPort

	def getEmailUserName: String = emailUserName

	def getEmailPassword: String = emailPassword

	def getEmailForceBcc: String = emailForceBcc

	def getEncryptionSecret: String = encryptionSecret

	def getHostname: String = hostname

	def getLoginPath: String = loginPath

	def getEmailValidatorCheckDns: Boolean = emailValidatorCheckDns

	def getHCaptchaSecret: String = hCaptchaSecret

	def getHCaptchaSiteKey: String = hCaptchaSiteKey
}