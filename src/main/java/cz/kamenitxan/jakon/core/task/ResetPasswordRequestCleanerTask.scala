package cz.kamenitxan.jakon.core.task

import java.sql.Date
import java.util.concurrent.TimeUnit

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.webui.entity.ResetPasswordEmailEntity

class ResetPasswordRequestCleanerTask extends AbstractTask(6, TimeUnit.HOURS) {
	private implicit val cls: Class[ResetPasswordEmailEntity] =  classOf[ResetPasswordEmailEntity]

	// language=SQL
	private val sql = "SELECT * FROM ResetPasswordEmailEntity where expirationDate <= ?"

	override def start(): Unit = {
		DBHelper.withDbConnection(implicit conn => {
			val stmt = conn.prepareStatement(sql)
			stmt.setDate(1, new Date(new java.util.Date().getTime))
			val expired = DBHelper.selectDeep(stmt)
			expired.foreach(_.delete())
		})
	}
}
