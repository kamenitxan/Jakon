package cz.kamenitxan.jakon

import java.sql.Connection
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.core.service.EmailTemplateService
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity

import java.util.Locale

object JakonInitializer {

	def init(): Unit = {
		implicit val conn: Connection = DBHelper.getConnection
		try {
			val masterAdminStmt = conn.prepareStatement("SELECT * FROM AclRule WHERE masterAdmin = ?")
			masterAdminStmt.setBoolean(1, true)
			val masterAdmin = DBHelper.selectSingleDeep(masterAdminStmt)(implicitly, classOf[AclRule])
			val acl = if (masterAdmin == null) {
				val acl = new AclRule()
				acl.name = "Admin"
				acl.masterAdmin = true
				acl.adminAllowed = true
				val aclId = acl.create()
				acl.id = aclId
				acl
			} else {
				masterAdmin
			}

			val userCount = DBHelper.count("SELECT count(*) FROM JakonUser")
			if (userCount == 0) {
				val user = new JakonUser()
				user.firstName = "Admin"
				user.lastName = "Admin"
				user.username = "admin"
				user.email = "admin@admin.cz"
				user.password = "admin"
				user.enabled = true
				user.emailConfirmed = true
				user.acl = acl
				user.locale = new Locale("en", "us")
				user.create()
			}


			if (Settings.isEmailEnabled) {
				createDefaultEmailTemplates()(conn)
			}
		} finally {
			conn.close()
		}
	}

	private def createDefaultEmailTemplates()(implicit conn: Connection): AnyVal = {
		val tmpl = EmailTemplateService.getByName("REGISTRATION")
		if (tmpl == null) {
			val tmpl = Utils.getResourceFromJar("/templates/admin/email/registration.peb")
			val emailTemplateEntity = new EmailTemplateEntity()
			emailTemplateEntity.subject = "Jakon Registration"
			emailTemplateEntity.from = "admin@jakon.cz"
			emailTemplateEntity.name = "REGISTRATION"
			emailTemplateEntity.template = tmpl.getOrElse("registrationTemplate")
			emailTemplateEntity.create()
		}

		val tmpl2 = EmailTemplateService.getByName("FORGET_PASSWORD")
		if (tmpl2 == null) {
			val tmpl = Utils.getResourceFromJar("/templates/admin/email/forgetPassword.peb")
			val emailTemplateEntity = new EmailTemplateEntity()
			emailTemplateEntity.subject = "Forget password"
			emailTemplateEntity.from = "admin@jakon.cz"
			emailTemplateEntity.name = "FORGET_PASSWORD"
			emailTemplateEntity.template = tmpl.getOrElse("forgetPasswordTemplate")
			emailTemplateEntity.create()
		}


	}
}
