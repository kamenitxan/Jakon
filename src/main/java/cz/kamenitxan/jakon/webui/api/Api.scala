package cz.kamenitxan.jakon.webui.api

import java.io.File

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonFile, JakonObject}
import cz.kamenitxan.jakon.webui.api.objects.{GetImagesRequest, GetImagesResponse, SearchRequest, SearchResponse}
import cz.kamenitxan.jakon.webui.controller.impl.FileManagerController
import spark.{Request, Response}

import scala.language.postfixOps


/**
 * Created by TPa on 29.04.18.
 */
object Api {
	val gson = new Gson()

	def search(req: Request, res: Response): SearchResponse = {
		val jsonReq = gson.fromJson(req.body(), classOf[SearchRequest])
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(jsonReq.objectName)).head

		val conn = DBHelper.getConnection
		try {
			if (jsonReq.query.isEmpty) {
				val sql = s"SELECT * FROM ${objectClass.getSimpleName}"
				val stmt = conn.createStatement()
				val res = DBHelper.select(stmt, sql, objectClass)
				val objects = res.map(r => r.entity)
				return new SearchResponse(true, objects)
			}

			// search by id
			try {
				val objectId = jsonReq.query.toInt
				val sql = s"SELECT * FROM ${objectClass.getSimpleName} WHERE id = ?"
				val stmt = conn.prepareStatement(sql)
				stmt.setInt(1, objectId)
				val res = DBHelper.selectSingle(stmt, objectClass)
				if (res.entity != null) {
					return new SearchResponse(true, List(res.entity))
				}
			} catch {
				case _: NumberFormatException =>
			}
		} finally {
			conn.close()
		}
		new SearchResponse(false, List[JakonObject]())
	}

	def getImages(req: Request, res: Response): GetImagesResponse = {
		val jsonReq = gson.fromJson(req.body(), classOf[GetImagesRequest])

		val path = Option.apply(jsonReq.path)
			.getOrElse(FileManagerController.REPOSITORY_BASE_PATH)
			.patch(FileManagerController.REPOSITORY_BASE_PATH.length, "/basePath", 0)
			.replace("/", File.separator)

		val files = DBHelper.withDbConnection(implicit conn => {
			val sql = "SELECT * FROM JakonFile WHERE (fileType = \"FOLDER\" OR fileType = \"IMAGE\") AND path = ?"
			val stmt = conn.prepareStatement(sql)
			stmt.setString(1, path)
			DBHelper.select(stmt, classOf[JakonFile]).map(_.entity)
		}).map(f => {
			// TODO: neni tady potreba File.separator, protoze se uklada do db dle systemu ?
			f.path = f.path.replace("basePath", "").replace("\\\\", "/").replace("\\", "/")
			if (!f.path.endsWith("/")) {
				f.path += "/"
			}
			f
		})
		new GetImagesResponse(true, files)
	}
}
