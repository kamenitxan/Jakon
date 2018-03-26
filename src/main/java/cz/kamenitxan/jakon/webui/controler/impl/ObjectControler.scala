package cz.kamenitxan.jakon.webui.controler.impl

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import javax.persistence.criteria.{CriteriaQuery, Expression, Root, Selection}
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConverters._

/**
  * Created by TPa on 08.09.16.
  */
object ObjectControler {
	val excludedFields = List("url", "sectionName", "objectSettings", "childClass")

	val pageNumber = 1
	val pageSize = 2

	def getList(req: Request, res: Response): ModelAndView = {
		val objectName = req.params(":name")
		val objectClass = DBHelper.getDaoClasses.find(c => c.getName.contains(objectName))
		if (objectClass.isDefined) {
			val session = DBHelper.getSession
			try {
				val criteriaBuilder = session.getCriteriaBuilder

				// pocet objektu
				val countQuery = criteriaBuilder.createQuery(classOf[java.lang.Long])
				val root = countQuery.from(classOf[JakonObject])
				countQuery.select(criteriaBuilder.count(root))
				val count = session.createQuery(countQuery).getSingleResult

				// seznam objektu
				/*val cls = classOf[JakonObject]
				val ocls: java.lang.Class[_ <: JakonObject] = objectClass.get

				val criteriaQuery = criteriaBuilder.createQuery(ocls)
				val from = criteriaQuery.from(ocls)
				val select = criteriaQuery.select(from)
				val typedQuery = session.createQuery(select)
				typedQuery.setFirstResult((pageNumber - 1) * pageSize)
				typedQuery.setMaxResults(pageNumber * pageSize )
				val page = typedQuery.getResultList
				print(page)*/
			} finally {
				session.close()
			}

			val objects = DBHelper.getSession.createCriteria(objectClass.get).list()
			val fields = Utils.getFieldsUpTo(objectClass.get, classOf[Object]).map(f => f.getName).filter(n => !excludedFields.contains(n)).asJava
			new Context(Map[String, Any](
				"objectName" -> objectName,
				"objects" -> objects,
				"fields" -> fields
			), "objects/list")
		} else {
			// TODO: osetri neexistujici objekt
			new Context(Map[String, Any](), "objects/list")
		}
	}


	def getItem(req: Request, res: Response) = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			obj = Option(DBHelper.getSession.get(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
		} else {
			obj = objectClass.newInstance()
		}
		//TODO: moznost udelat promene required
		val fields = Utils.getFieldsUpTo(objectClass, classOf[Object]).filter(n => !excludedFields.contains(n.getName))
		val f = FieldConformer.getFieldInfos(obj, fields).asJava
		new Context(Map[String, Any](
			"objectName" -> objectName,
			"object" -> obj,
			"id" -> obj.id,
			"fields" -> f
		), "objects/single")
	}

	def updateItem(req: Request, res: Response) = {
		val params = req.queryParams().asScala
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			val session = DBHelper.getSession
			obj = Option(session.load(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
			session.close()
		} else {
			obj = objectClass.newInstance()
		}

		for (p <- params.filter(p => !p.equals("id"))) {
			val fieldRef = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.equals(p)).get
			fieldRef.setAccessible(true)
			val ftype = fieldRef.getType
			val value = req.queryParams(p).conform(ftype)
			if (value != null) {
				fieldRef.set(obj, value)
			}
		}
		if (objectId.nonEmpty) {
			obj.update()
		} else {
			obj.create()
		}
		res.redirect("/admin/object/" + objectName)
		new Context(Map[String, Any](), "objects/list")
	}

	def deleteItem(req: Request, res: Response) = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt.get
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		val obj = DBHelper.getSession.load(objectClass, objectId)
		obj.delete()
		res.redirect("/admin/object/" + objectName)
		new Context(Map[String, Any](), "objects/list")
	}
}
