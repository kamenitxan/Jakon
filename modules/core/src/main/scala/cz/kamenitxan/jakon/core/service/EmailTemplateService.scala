package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.mail.EmailTemplateEntity

import java.sql.Connection

/**
 * Created by TPa on 20.06.2021.
 */
object EmailTemplateService {

	implicit val cls: Class[EmailTemplateEntity] = classOf[EmailTemplateEntity]

	// language=SQL
	private val BY_NAME_SQL = "SELECT id, name, addressFrom, template, subject FROM EmailTemplateEntity WHERE name = ?"
	def getByName(name: String)(implicit conn: Connection): EmailTemplateEntity = {
		val stmt = conn.prepareStatement(BY_NAME_SQL)
		stmt.setString(1, name)
		DBHelper.selectSingleDeep(stmt)
	}

}
