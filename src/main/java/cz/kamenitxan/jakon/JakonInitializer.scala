package cz.kamenitxan.jakon

import java.sql.Connection

import cz.kamenitxan.jakon.core.Director.SELECT_EMAIL_TMPL_SQL
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity

object JakonInitializer {

	def init(): Unit = {
		implicit val conn: Connection = DBHelper.getConnection
		try {
			val masterAdminStmt = conn.prepareStatement("SELECT * FROM AclRule WHERE masterAdmin = ?")
			masterAdminStmt.setBoolean(1, true)
			val masterAdmin = DBHelper.selectSingleDeep(masterAdminStmt, classOf[AclRule])
			var acl: AclRule = null
			if (masterAdmin == null) {
				acl = new AclRule()
				acl.name = "Admin"
				acl.masterAdmin = true
				acl.adminAllowed = true
				val aclId = acl.create()
				acl.id = aclId
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
				user.create()
			}


			if (Settings.isEmailEnabled) {
				createDefaultEmailTemplates()(conn)
			}
		} finally {
			conn.close()
		}
	}

	def createDefaultEmailTemplates()(conn: Connection) = {
		val stmt = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
		stmt.setString(1, "REGISTRATION")
		val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity
		if (tmpl == null) {
			val tmpl = Utils.getResourceFromJar("/templates/admin/email/registration.peb")
			val emailTemplateEntity = new EmailTemplateEntity()
			emailTemplateEntity.subject = "Jakon Registration"
			emailTemplateEntity.from = "admin@jakon.cz"
			emailTemplateEntity.name = "REGISTRATION"
			emailTemplateEntity.template = tmpl.getOrElse("registrationTemplate")
			emailTemplateEntity.create()
		}

		val stmt2 = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
		stmt2.setString(1, "FORGET_PASSWORD")
		val tmpl2 = DBHelper.selectSingle(stmt2, classOf[EmailTemplateEntity]).entity
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
