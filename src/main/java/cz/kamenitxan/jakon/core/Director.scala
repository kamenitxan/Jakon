package cz.kamenitxan.jakon.core

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.controler.IControler
import cz.kamenitxan.jakon.core.customPages.CustomPage
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.Dao.DBHelper.getSession
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.core.template.{Pebble, TemplateUtils}
import cz.kamenitxan.jakon.utils.mail.{EmailEntity, EmailTemplateEntity}
import cz.kamenitxan.jakon.webui.Routes
import cz.kamenitxan.jakon.webui.controler.impl.Authentication
import org.hibernate.criterion.Restrictions
import org.slf4j.{Logger, LoggerFactory}


/**
  * Created by Kamenitxan (kamenitxan@me.com) on 05.12.15.
  */
object Director {
	var customPages: List[IControler] = List[IControler]()
	var controllers: List[IControler] = List[IControler]()
	final private val logger: Logger = LoggerFactory.getLogger(this.getClass)

	def init() {
		Settings.setTemplateDir("templates/bacon/")
		Settings.setTemplateEngine(new Pebble)
		Settings.setOutputDir("out")
		Settings.setDatabaseDriver("org.sqlite.JDBC")
		Settings.setDatabaseConnPath("jdbc:sqlite:jakon.sqlite")
		//MongoHelper.setDbName("jakon")
	}

	def start(): Unit = {
		logger.info("Jakon started")
		Routes.init()
		DBHelper.addDao(new JakonUser().getClass)
		val adminUser = DBHelper.getUserDao.findAll()
		if (adminUser == null || adminUser.isEmpty) {
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
			user.acl = acl
			Authentication.createUser(user)
		}

		getSession.beginTransaction()
		val criteria = getSession.createCriteria(classOf[EmailTemplateEntity])
		val tmpl = criteria.add(Restrictions.eq("name", "REGISTRATION") ).uniqueResult().asInstanceOf[EmailTemplateEntity]
		if (tmpl == null) {
			val emailTemplateEntity = new EmailTemplateEntity()
			emailTemplateEntity.subject = "Jakon Registration"
			emailTemplateEntity.from = "admin@jakon.cz"
			emailTemplateEntity.name = "REGISTRATION"
			emailTemplateEntity.template = "registration"
			emailTemplateEntity.create()
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

	def registerCustomPage(page: CustomPage) {
		customPages = customPages.::(page)

	}

	def registerControler(controler: IControler) {
		controllers = controllers.::(controler)
	}
}