package cz.kamenitxan.jakon.webui.api

import com.google.gson.Gson
import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.api.objects.{SearchRequest, SearchResponse}
import javax.persistence.criteria.{CriteriaQuery, Root}
import spark.{Request, Response}

import scala.collection.JavaConverters._
import scala.language.postfixOps


/**
  * Created by TPa on 29.04.18.
  */
object Api {

	def search(req: Request, res: Response): SearchResponse = {
		val gson = new Gson()
		val jsonReq = gson.fromJson(req.body(), classOf[SearchRequest])
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(jsonReq.objectName)).head

		/*
		val session = DBHelper.getSession
		session.beginTransaction()
		try {
			// search all
			if (jsonReq.query.isEmpty) {
				val ocls: Class[JakonObject] = objectClass.asInstanceOf[Class[JakonObject]]

				val criteriaBuilder = session.getCriteriaBuilder
				val criteriaQuery: CriteriaQuery[JakonObject] = criteriaBuilder.createQuery(ocls)
				val from: Root[JakonObject] = criteriaQuery.from(ocls)
				val select: CriteriaQuery[JakonObject] = criteriaQuery.select(from)
				val typedQuery = session.createQuery(select)
				typedQuery.setMaxResults(10)
				val pageItems = typedQuery.getResultList
				return new SearchResponse(true, pageItems)
			}

			// search by id
			try {
				val objectId = jsonReq.query.toInt
				val session = DBHelper.getSession
				session.beginTransaction()
				try {
					val obj: JakonObject = session.get(objectClass, objectId)
					if (obj != null) {
						return new SearchResponse(true, List(obj) asJava)
					}
				} finally {
					session.getTransaction.commit()
				}
			} catch {
				case _: NumberFormatException =>
			}
		} finally {
			session.getTransaction.commit()
			session.close()
		}

        */

		new SearchResponse(true, List[JakonObject]() asJava)
	}
}
