package cz.kamenitxan.jakon.core.model.service

import java.sql.Connection

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonUser

object UserService {

	def getMasterAdmin()(implicit conn: Connection): JakonUser = {
		val sql = "SELECT * FROM JakonUser JOIN AclRule AR ON JakonUser.acl_id = AR.id WHERE AR.masterAdmin = 1 ORDER BY AR.id LIMIT 1;"
		val stmt = conn.createStatement()
		DBHelper.selectSingleDeep(stmt, sql, classOf[JakonUser])
	}
}
