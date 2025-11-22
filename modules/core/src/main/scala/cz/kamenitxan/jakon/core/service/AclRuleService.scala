package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.AclRule

import java.sql.Connection

/**
 * Created by TPa on 27.01.2022.
 */
object AclRuleService {
	implicit val cls: Class[AclRule] = classOf[AclRule]

	// language=SQL
	private val BY_NAME_SQL = "SELECT * FROM AclRule WHERE name = ?"
	def getByName(name: String)(implicit conn: Connection): Option[AclRule] = {
		val stmt = conn.prepareStatement(BY_NAME_SQL)
		stmt.setString(1, name)
		Option.apply(DBHelper.selectSingleDeep(stmt))
	}

	def createIfNotExists(rule: AclRule)(implicit conn: Connection): Unit = {
		val existing = getByName(rule.name)
		if (existing.isEmpty) {
			rule.create()
		}
	}
}
