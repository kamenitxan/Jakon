package cz.kamenitxan.jakon.core

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.customPages.AbstractCustomPage
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.core.task.TaskRunner
import cz.kamenitxan.jakon.core.template.{Pebble, TemplateUtils}
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity
import cz.kamenitxan.jakon.webui.Routes
import org.slf4j.{Logger, LoggerFactory}


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	var customPages: List[IControler] = List[IControler]()
	var controllers: List[IControler] = List[IControler]()
	final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	val SELECT_EMAIL_TMPL_SQL = "SELECT addressFrom, template FROM EmailTemplateEntity WHERE name = ?"

	def init() {
		Settings.setTemplateDir("templates/bacon/")
		Settings.setTemplateEngine(new Pebble)
		Settings.setOutputDir("out")
		Settings.setDatabaseDriver("org.sqlite.JDBC")
		Settings.setDatabaseConnPath("jdbc:sqlite:jakon.sqlite")
		//MongoHelper.setDbName("jakon")
	}

	def start(): Unit = {
		DBHelper.addDao(new JakonUser().getClass)
		DBHelper.createSessionFactory()
		DBHelper.createTables()

		TaskRunner.startTaskRunner()
		logger.info("Jakon started")
		Routes.init()


		val conn = DBHelper.getConnection
		try {
			val usr_stmt = conn.createStatement()
			val rs = usr_stmt.executeQuery("SELECT count(*) FROM JakonUser")
			val userCount = rs.getInt(1)
			usr_stmt.close()
			if (userCount == 0) {
				val acl = new AclRule()
				acl.name = "Admin"
				acl.masterAdmin = true
				acl.adminAllowed = true
				acl.create()

				val user = new JakonUser()
				user.firstName = "Admin"
				user.lastName = "Admin"
				user.username = "admin"
				user.email = "admin@admin.cz"
				user.password = "admin"
				user.enabled = true
				user.emailConfirmed = true
				user.acl = acl
				user.create()
			}

			val stmt = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
			stmt.setString(1, "REGISTRATION")
			val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity.asInstanceOf[EmailTemplateEntity]
			if (tmpl == null) {
				val emailTemplateEntity = new EmailTemplateEntity()
				emailTemplateEntity.subject = "Jakon Registration"
				emailTemplateEntity.from = "admin@jakon.cz"
				emailTemplateEntity.name = "REGISTRATION"
				emailTemplateEntity.template = "registration"
				emailTemplateEntity.create()
			}

			val stmt2 = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
			stmt2.setString(1, "FORGET_PASSWORD")
			val tmpl2 = DBHelper.selectSingle(stmt2, classOf[EmailTemplateEntity]).entity.asInstanceOf[EmailTemplateEntity]
			if (tmpl2 == null) {
				val emailTemplateEntity = new EmailTemplateEntity()
				emailTemplateEntity.subject = "Forget password"
				emailTemplateEntity.from = "admin@jakon.cz"
				emailTemplateEntity.name = "FORGET_PASSWORD"
				emailTemplateEntity.template = "forgetPassword"
				emailTemplateEntity.create()
			}
		} finally {
			conn.close()
		}

		logger.info("Jakon default init complete")
	}

	def render() {
		TemplateUtils.clean(Settings.getOutputDir)
		controllers.foreach(i => {
			i.generateRun()
		})
		customPages.foreach(i => {
			i.generateRun()
		})

		if (Settings.getStaticDir != null && Settings.getOutputDir != null) {
			TemplateUtils.copy(Settings.getStaticDir, Settings.getOutputDir)
		}
		//TODO: moznost vypnout administraci
		//TemplateUtils.copy("templates/admin/static", Settings.getOutputDir)
		logger.info("Render complete")
	}

	def registerCustomPage(page: AbstractCustomPage) {
		customPages = customPages.::(page)

	}

	def registerControler(controler: IControler) {
		controllers = controllers.::(controler)
	}
}