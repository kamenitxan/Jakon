package cz.kamenitxan.jakon.webui.controler.impl

import java.lang.reflect.Field
import java.sql.Connection
import java.util.Collections

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.{BasicJakonObject, JakonObject, Ordered}
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import javax.persistence.criteria.{CriteriaQuery, Root}
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConverters._
import scala.collection.mutable
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
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName))
		if (objectClass.isDefined) {
			if (!isAuthorized(objectClass.get)) {
				return new Context(Map[String, Any](
					"objectName" -> objectName
				), "pages/unauthorized")
			}
			implicit val session = DBHelper.getSession
			implicit val conn = DBHelper.getConnection
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
				val select = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					criteriaQuery.select(from).orderBy(criteriaBuilder.asc(from.get("objectOrder")))
				} else {
					criteriaQuery.select(from)
				}
				val typedQuery = session.createQuery(select)
				val first = (pageNumber - 1) * pageSize
				typedQuery.setFirstResult(first)
				typedQuery.setMaxResults(10)
				val pageItems = if(ocls.getInterfaces.contains(classOf[Ordered])) {
					fetchVisibleOrder(typedQuery.getResultList, ocls)
				} else {
					typedQuery.getResultList
				}

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
				conn.close()
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

		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), "pages/unauthorized")
		}
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			implicit val session = DBHelper.getSession
			implicit val conn = DBHelper.getConnection
			session.beginTransaction()
			try {
				obj = Option(session.get(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
				if (obj.getClass.getInterfaces.contains(classOf[Ordered])) {
					fetchVisibleOrder(obj, objectClass)
				}
			} finally {
				session.getTransaction.commit()
				session.close()
				conn.close()
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
		val params: mutable.Set[String] = req.queryParams().asScala
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head

		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), "pages/unauthorized")
		}
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			val session = DBHelper.getSession
			session.beginTransaction()
			obj = Option(session.find(objectClass, objectId.get)).getOrElse(objectClass.newInstance())
			session.getTransaction.commit()
		} else {
			obj = objectClass.newInstance()
		}

		var formOrder = 0
		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRef: Field = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.equals(p)).get
			fieldRef.setAccessible(true)
			val value = req.queryParams(p).conform(fieldRef)
			if (value != null) {
				p match {
					case "password" => {
						if (!value.asInstanceOf[String].startsWith("$2a$")) {
							fieldRef.set(obj, value)
						}
					}
					case "visibleOrder" => formOrder = value.asInstanceOf[Int]
					case "objectOrder" =>
					case _ => fieldRef.set(obj, value)
				}
			}
		}
		if (objectId.nonEmpty) {
			obj = updateOrder(objectClass, obj, formOrder)
			obj.update()
		} else {
			val id = obj.create()
			updateNewOrder(objectClass, obj, id)
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

	private def fetchVisibleOrder(objects: java.util.List[JakonObject], objectClass: Class[_])(implicit conn: Connection): java.util.List[JakonObject] = {
		val stmt = conn.createStatement()
		val result = DBHelper.select(stmt, "SELECT id FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC", classOf[BasicJakonObject])
		var i = 0
		val allObjects = result.map(qr => {
			i += 1
			(qr.entity.asInstanceOf[Int], i)
		}).toMap
		objects.forEach(o => o.asInstanceOf[JakonObject with  Ordered].visibleOrder = allObjects(o.id))
		objects
	}

	private def fetchVisibleOrder(obj: JakonObject, objectClass: Class[_])(implicit conn: Connection): JakonObject = {
		fetchVisibleOrder(Collections.singletonList(obj), objectClass).get(0)
	}

	private def updateNewOrder(objectClass: Class[_], obj: JakonObject, id: Int): Unit = {
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			return
		}
		val session = DBHelper.getSession
		session.beginTransaction()
		val query = session.createNativeQuery("SELECT objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder DESC LIMIT 1")
		val lastOrder = query.getSingleResult.asInstanceOf[Double]
		obj.asInstanceOf[JakonObject with Ordered].setObjectOrder(lastOrder + 10)
		session.getTransaction.commit()
		obj.update()
	}

	private def updateOrder(objectClass: Class[_], objs: JakonObject, formOrder: Int): JakonObject = {
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			return objs
		}
		val obj = objs.asInstanceOf[JakonObject with Ordered]
		implicit val session = DBHelper.getSession
		implicit val conn = DBHelper.getConnection
		session.beginTransaction()
		val persistedObj = session.get(objectClass, obj.id).asInstanceOf[JakonObject with Ordered]
		fetchVisibleOrder(persistedObj, objectClass)
		if (formOrder == persistedObj.visibleOrder) {
			session.getTransaction.commit()
			session.close()
			return obj
		}

		val query = session.createNativeQuery("SELECT id, objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC")
		val allObjects = query.getResultList.asScala.map(o => {
			val arr = o.asInstanceOf[Array[Any]]
			(arr(0).asInstanceOf[Int], arr(1).asInstanceOf[Double])
		}).filter(t => t._1 != obj.id).toVector
		session.getTransaction.commit()
		session.close()
		conn.close()

		val requiredOrder = if (formOrder < 0) 0 else if (formOrder > allObjects.size) allObjects.size else formOrder - 1
		val earlier = if (allObjects.lift(requiredOrder-1).isDefined && allObjects.lift(requiredOrder-1).get._1 != obj.id) {
			allObjects.lift(requiredOrder-1)
		} else {
			Option.empty
		}
		val latter = if (allObjects.lift(requiredOrder).isDefined && allObjects.lift(requiredOrder).get._1 != obj.id) {
			allObjects.lift(requiredOrder)
		} else {
			Option.empty
		}

		val resultPos = if (earlier.isDefined && latter.isDefined) {
			(latter.get._2 + earlier.get._2) / 2.0
		} else if (earlier.isDefined && latter.isEmpty) {
			earlier.get._2 + 10
		} else if(earlier.isEmpty && latter.isDefined) {
			latter.get._2 / 2
		} else {
			10
		}
		obj.objectOrder = resultPos
		obj
	}
}
