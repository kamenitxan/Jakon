package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.KeyValueEntity

import java.sql.Connection

/**
  * Created by TPa on 2019-07-03.
  */
object KeyValueService {

	def getByKey(name: String)(implicit conn: Connection): Option[KeyValueEntity] = {
		val sql = "SELECT * FROM KeyValueEntity WHERE name = ? LIMIT 1"
		val stmt = conn.prepareStatement(sql)
		stmt.setString(1, name)
		val res = DBHelper.selectSingle(stmt, classOf[KeyValueEntity])
		if (res.entity != null) {
			Option.apply(res.entity)
		} else {
			Option.empty
		}
	}

	def deleteByKey(name: String)(implicit conn: Connection): Unit = {
		val okve = getByKey(name)
		okve.foreach(kve => kve.delete())
	}

}
