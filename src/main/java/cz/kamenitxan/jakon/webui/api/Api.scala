package cz.kamenitxan.jakon.webui.api

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.api.objects.{SearchRequest, SearchResponse}
import spark.{Request, Response}

import scala.language.postfixOps


/**
  * Created by TPa on 29.04.18.
  */
object Api {

	def search(req: Request, res: Response): SearchResponse = {
		val gson = new Gson()
		val jsonReq = gson.fromJson(req.body(), classOf[SearchRequest])
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(jsonReq.objectName)).head

		val conn = DBHelper.getConnection
		try {
			if (jsonReq.query.isEmpty) {
				val sql = s"SELECT * FROM ${objectClass.getSimpleName} LIMIT 10"
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

	def getImages() = {
		// TODO: get images endpoint
	}
}
