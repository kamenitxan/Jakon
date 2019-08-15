package cz.kamenitxan.jakon

import java.sql.Connection

import cz.kamenitxan.jakon.core.Director.SELECT_EMAIL_TMPL_SQL
import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{AclRule, JakonUser}
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity

object JakonInitializer {

	def init() = {
		val conn = DBHelper.getConnection
		try {
			val usr_stmt = conn.createStatement()
			val rs = usr_stmt.executeQuery("SELECT count(*) FROM JakonUser")
			rs.next()
			val userCount = rs.getInt(1)
			usr_stmt.close()
			if (userCount == 0) {
				val acl = new AclRule()
				acl.name = "Admin"
				acl.masterAdmin = true
				acl.adminAllowed = true
				val aclId = acl.create()
				acl.id = aclId

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
				val stmt = conn.prepareStatement(SELECT_EMAIL_TMPL_SQL)
				stmt.setString(1, "REGISTRATION")
				val tmpl = DBHelper.selectSingle(stmt, classOf[EmailTemplateEntity]).entity
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
				val tmpl2 = DBHelper.selectSingle(stmt2, classOf[EmailTemplateEntity]).entity
				if (tmpl2 == null) {
					val emailTemplateEntity = new EmailTemplateEntity()
					emailTemplateEntity.subject = "Forget password"
					emailTemplateEntity.from = "admin@jakon.cz"
					emailTemplateEntity.name = "FORGET_PASSWORD"
					emailTemplateEntity.template = "forgetPassword"
					emailTemplateEntity.create()
				}
			}
			createDefaultEmailTemplates()
		} finally {
			conn.close()
		}
	}

	def createDefaultEmailTemplates()(conn: Connection) = {
		val resourceDir = this.getClass.getResourceAsStream(s"/templates/defaultEmailTemplates")
	}
}
