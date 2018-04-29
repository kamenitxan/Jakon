package cz.kamenitxan.jakon.webui.api

import java.lang.NumberFormatException

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.api.objects.{SearchRequest, SearchResponse}
import spark.{Request, Response}

/**
  * Created by TPa on 29.04.18.
  */
object Api {

	def search(req: Request, res: Response): SearchResponse = {
		val gson = new Gson()
		val jsonReq = gson.fromJson(req.body(), classOf[SearchRequest])
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(jsonReq.objectName)).head
		try {
			val objectId = jsonReq.query.toInt
			val session = DBHelper.getSession
			session.beginTransaction()
			try {
				val obj: JakonObject = session.get(objectClass, objectId)
				if (obj != null) {
					return new SearchResponse(true, List(obj))
				}
			} finally {
				session.getTransaction.commit()
			}
		} catch {
			case _: NumberFormatException =>
		}

		new SearchResponse(true, null)
	}
}
