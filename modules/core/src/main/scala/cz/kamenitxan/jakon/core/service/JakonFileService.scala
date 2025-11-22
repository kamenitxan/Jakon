package cz.kamenitxan.jakon.core.service

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonFile

import java.sql.Connection

object JakonFileService {
	private implicit val cls: Class[JakonFile] = classOf[JakonFile]

	// language=SQL
	private val GET_IMAGES = "SELECT * FROM JakonFile WHERE fileType = ?"
	// language=SQL
	private val GET_BY_PATH = "SELECT * FROM JakonFile WHERE path = ? AND name = ?"


	def getAll(implicit conn: Connection): Seq[JakonFile] = {
		val sql = "SELECT * FROM JakonFile"
		val stmt = conn.createStatement()
		DBHelper.selectDeep(stmt, sql)
	}

	def getById(id: Int)(implicit conn: Connection): Option[JakonFile] = {
		val sql = "SELECT * FROM JakonFile WHERE id = ?"
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, id)
		val res = DBHelper.selectSingleDeep(stmt)
		Option.apply(res)
	}

	def getImages()(implicit conn: Connection): Seq[JakonFile] = {
		val stmt = conn.createStatement()
		DBHelper.select(stmt, GET_IMAGES, classOf[JakonFile]).map(qr => qr.entity)
	}

	def getByPath(path: String, name: String)(implicit conn: Connection): Option[JakonFile] = {
		val stmt = conn.prepareStatement(GET_BY_PATH)
		stmt.setString(1, path)
		stmt.setString(2, name)
		val res = DBHelper.selectSingle(stmt, classOf[JakonFile]).entity
		Option.apply(res)
	}
}
