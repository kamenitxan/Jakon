package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonFile

import java.sql.Connection

object JakonFileService {
	private implicit val cls: Class[JakonFile] = classOf[JakonFile]

	// language=SQL
	val GET_IMAGES = "SELECT * FROM JakonFile WHERE fileType = ?"


	def getAll(implicit conn: Connection): Seq[JakonFile] = {
		val sql = "SELECT * FROM JakonFile"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}

	def getById(id: Int)(implicit conn: Connection): JakonFile = {
		val sql = "SELECT * FROM JakonFile WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, id)
		DBHelper.selectSingleDeep(stmt)
	}

	def getImages()(implicit conn: Connection): Seq[JakonFile] = {
		val stmt = conn.createStatement()
		DBHelper.select(stmt, GET_IMAGES, classOf[JakonFile]).map(qr => qr.entity)
	}
}
