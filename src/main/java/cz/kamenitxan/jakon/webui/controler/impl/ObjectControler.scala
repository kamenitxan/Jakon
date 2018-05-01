package cz.kamenitxan.jakon.webui.controler.impl

import java.lang.reflect.Field
import javax.persistence.criteria.{CriteriaQuery, Root}

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object ObjectControler {
	val excludedFields = List("url", "sectionName", "objectSettings", "childClass")

	val pageSize = 10

	def getList(req: Request, res: Response): ModelAndView = {
		val objectName = req.params(":name")
		val page = req.queryParams("page")
		val pageNumber = Try(Integer.parseInt(page)).getOrElse(1)
		val objectClass = DBHelper.getDaoClasses.find(c => c.getName.contains(objectName))
		if (objectClass.isDefined) {
			if (!isAuthorized(objectClass.get)) {
				return new Context(Map[String, Any](
					"objectName" -> objectName
				), "pages/unauthorized")
			}
			val session = DBHelper.getSession
			try {
				session.beginTransaction()
				val criteriaBuilder = session.getCriteriaBuilder

				// pocet objektu
				val countQuery = criteriaBuilder.createQuery(classOf[java.lang.Long])
				val root = countQuery.from(objectClass.get)
				countQuery.select(criteriaBuilder.count(root))
				val count = session.createQuery(countQuery).getSingleResult

				// seznam objektu
				val ocls: Class[JakonObject] = objectClass.get.asInstanceOf[Class[JakonObject]]

				val criteriaQuery: CriteriaQuery[JakonObject] = criteriaBuilder.createQuery(ocls)
				val from: Root[JakonObject] = criteriaQuery.from(ocls)
				val select: CriteriaQuery[JakonObject] = criteriaQuery.select(from)
				val typedQuery = session.createQuery(select)
				val first = (pageNumber - 1) * pageSize
				typedQuery.setFirstResult(first)
				typedQuery.setMaxResults(10)
				val pageItems = typedQuery.getResultList

				//val objects = DBHelper.getSession.createCriteria(objectClass.get).list()
				val fields = Utils.getFieldsUpTo(objectClass.get, classOf[Object]).filter(n => !excludedFields.contains(n.getName))
				new Context(Map[String, Any](
					"objectName" -> objectName,
					"objects" -> pageItems,
					"pageNumber" -> pageNumber,
					"pageCount" -> Math.max(Math.ceil(count / pageSize.toFloat), 1),
					"objectCount" -> count,
					"fields" -> FieldConformer.getEmptyFieldInfos(fields)
				), "objects/list")
			} finally {
				session.getTransaction.commit()
				session.close()
			}
		} else {
			// TODO: osetri neexistujici objekt
			new Context(Map[String, Any](), "objects/list")
		}
	}


	def getItem(req: Request, res: Response): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			if (!isAuthorized(objectClass)) {
				return new Context(Map[String, Any](
					"objectName" -> objectName
				), "pages/unauthorized")
			}
			val session = DBHelper.getSession
			session.beginTransaction()
			try {
				obj = Option(session.get(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
			} finally {
				session.getTransaction.commit()
			}
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

	def updateItem(req: Request, res: Response): Context = {
		val params = req.queryParams().asScala
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			if (!isAuthorized(objectClass)) {
				return new Context(Map[String, Any](
					"objectName" -> objectName
				), "pages/unauthorized")
			}
			val session = DBHelper.getSession
			session.beginTransaction()
			obj = Option(session.find(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
			session.getTransaction.commit()
		} else {
			obj = objectClass.newInstance()
		}

		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRef: Field = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.equals(p)).get
			fieldRef.setAccessible(true)
			val value = req.queryParams(p).conform(fieldRef)
			if (value != null) {
				if (p.equals("password")) {
					if (!value.asInstanceOf[String].startsWith("$2a$")) {
						fieldRef.set(obj, value)
					}
				} else {
					fieldRef.set(obj, value)
				}
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

	def deleteItem(req: Request, res: Response): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt.get
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), "pages/unauthorized")
		}
		val session = DBHelper.getSession
		session.beginTransaction()
		val obj = session.load(objectClass, objectId)
		session.getTransaction.commit()
		obj.delete()
		res.redirect("/admin/object/" + objectName)
		new Context(Map[String, Any](), "objects/list")
	}

	private def isAuthorized(objectClass: Class[_]): Boolean = {
		val user = PageContext.getInstance().getLoggedUser
		if (user.isEmpty || user.get.acl.masterAdmin) {
			true
		} else {
			user.get.acl.allowedControllers.contains(objectClass.getCanonicalName)
		}
	}
}
