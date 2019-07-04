package cz.kamenitxan.jakon.core.service

import java.sql.Connection

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.KeyValueEntity

/**
  * Created by TPa on 2019-07-03.
  */
object KeyValueService {

	def getByKey(name: String)(implicit conn: Connection): Option[KeyValueEntity] = {
		val sql = ""
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		val res = DBHelper.selectSingle(stmt, classOf[KeyValueEntity])
		if (res.entity != null) {
			Option.apply(res.entity)
		} else {
			Option.empty
		}
	}
}
