package cz.kamenitxan.jakon.core.service

import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonFile

object JakonFileService {

	// language=SQL
	val GET_IMAGES = "SELECT * FROM JakonFile WHERE fileType = ?"

	def getAll(implicit conn: Connection): List[JakonFile] = {
		val sql = "SELECT * FROM JakonFile"
		val stmt = conn.createStatement()
		DBHelper.select(stmt, sql, classOf[JakonFile]).map(qr => qr.entity)
	}

	def getImages(path: String)(implicit conn: Connection): List[JakonFile] = {
		val stmt = conn.createStatement()
		DBHelper.select(stmt, GET_IMAGES, classOf[JakonFile]).map(qr => qr.entity)
	}
}
