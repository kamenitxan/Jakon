package cz.kamenitxan.jakon.webui.controler.impl

import java.lang.reflect.Field
import java.sql.Connection

import cz.kamenitxan.jakon.core.model.Dao.{DBHelper, QueryResult}
import cz.kamenitxan.jakon.core.model.{BasicJakonObject, JakonObject, Ordered}
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
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
			implicit val conn: Connection = DBHelper.getConnection
			try {
				// pocet objektu
				val countSql = s"SELECT count(*) FROM $objectName"
				val stmt = conn.createStatement()
				val rs = stmt.executeQuery(countSql)
				rs.next()
				val count = rs.getInt(1)

				// seznam objektu
				val ocls: Class[JakonObject] = objectClass.get.asInstanceOf[Class[JakonObject]]


				val first = (pageNumber - 1) * pageSize


				val order = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					s"ORDER BY $objectName.objectOrder"
				} else {
					""
				}
				val listSql = s"SELECT * FROM JakonObject INNER JOIN $objectName ON JakonObject.id = $objectName.id $order LIMIT $pageSize OFFSET $first"
				val stmt2 = conn.createStatement()
				val resultList = DBHelper.select(stmt2, listSql, ocls)
				// TODO: nacist foreign key objekty
				val pageItems: List[JakonObject] = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					fetchVisibleOrder(resultList.map(qr => qr.entity), ocls)
				} else {
					resultList.map(qr => qr.entity)
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
			implicit val conn = DBHelper.getConnection
			try {
				val stmt = conn.prepareStatement(s"SELECT * FROM $objectName INNER JOIN JakonObject ON JakonObject.id = $objectName.id WHERE $objectName.id = ?")
				stmt.setInt(1, objectId.get)
				obj = Option(DBHelper.selectSingle(stmt, objectClass).entity).getOrElse(objectClass.newInstance())
				if (obj.getClass.getInterfaces.contains(classOf[Ordered])) {
					fetchVisibleOrder(obj, objectClass)
				}
			} finally {
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
			val conn = DBHelper.getConnection
			val stmt = conn.prepareStatement("SELECT id FROM JakonObject WHERE id = ?")
			stmt.setInt(1, objectId.get)
			obj = Option(DBHelper.selectSingle(stmt, objectClass).entity).getOrElse(objectClass.newInstance())
			conn.close()
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

		val sql = "DELETE FROM JakonObject WHERE id = ?"
		val conn = DBHelper.getConnection
		val stmt = conn.prepareStatement(sql)
		stmt.setInt(1, objectId)
		stmt.executeUpdate()
		conn.close()

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

	private def fetchVisibleOrder(objects: List[JakonObject], objectClass: Class[_])(implicit conn: Connection): List[JakonObject] = {
		val stmt = conn.createStatement()
		val result = DBHelper.select(stmt, "SELECT id FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC", classOf[BasicJakonObject])
		var i = 0
		val allObjects = result.map(qr => {
			i += 1
			(qr.entity.asInstanceOf[BasicJakonObject].id, i)
		}).toMap
		objects.foreach(o => o.asInstanceOf[JakonObject with Ordered].visibleOrder = allObjects(o.id))
		objects
	}

	private def fetchVisibleOrder(obj: JakonObject, objectClass: Class[_])(implicit conn: Connection): JakonObject = {
		fetchVisibleOrder(obj :: Nil, objectClass).head
	}

	private def updateNewOrder(objectClass: Class[_], obj: JakonObject, id: Int): Unit = {
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			return
		}
		val fieldRef: Field = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.equals("objectOrder")).get
		fieldRef.setAccessible(true)

		val conn = DBHelper.getConnection
		val stmt = conn.createStatement()
		val rs =  stmt.executeQuery("SELECT objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder DESC LIMIT 1")

		if (rs.next()) {
			val lastOrder = rs.getInt(1)
			fieldRef.set(obj, lastOrder + 10)
		} else {
			fieldRef.set(obj, 10)
		}
		conn.close()
		obj.update()
	}

	private def updateOrder(objectClass: Class[_], objs: JakonObject, formOrder: Int): JakonObject = {
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			return objs
		}
		val obj = objs.asInstanceOf[JakonObject with Ordered]
		implicit val conn = DBHelper.getConnection
		val stmt = conn.prepareStatement("SELECT id, objectOrder FROM " + objectClass.getSimpleName + " WHERE id = ?")
		stmt.setInt(1, obj.id)
		val persistedObj: JakonObject with Ordered = DBHelper.selectSingle(stmt, classOf[BasicJakonObject]).entity.asInstanceOf[JakonObject with Ordered]
		fetchVisibleOrder(persistedObj, objectClass)
		if (formOrder == persistedObj.visibleOrder) {

			return obj
		}
		val stmt2 = conn.createStatement()
		val queryResult: List[QueryResult] = DBHelper.select(stmt2, "SELECT id, objectOrder FROM " + objectClass.getSimpleName + " ORDER BY objectOrder ASC", classOf[BasicJakonObject])
		val allObjects = queryResult.map(qr => {
			qr.entity.asInstanceOf[JakonObject with Ordered]
		}).filter(o => o.id != obj.id).toVector

		conn.close()

		val requiredOrder = if (formOrder < 0) 0 else if (formOrder > allObjects.size) allObjects.size else formOrder - 1
		val earlier = if (allObjects.lift(requiredOrder-1).isDefined && allObjects.lift(requiredOrder-1).get.id != obj.id) {
			allObjects.lift(requiredOrder-1)
		} else {
			Option.empty
		}
		val latter = if (allObjects.lift(requiredOrder).isDefined && allObjects.lift(requiredOrder).get.id != obj.id) {
			allObjects.lift(requiredOrder)
		} else {
			Option.empty
		}

		val resultPos = if (earlier.isDefined && latter.isDefined) {
			(latter.get.objectOrder + earlier.get.objectOrder) / 2.0
		} else if (earlier.isDefined && latter.isEmpty) {
			earlier.get.objectOrder + 10
		} else if(earlier.isEmpty && latter.isDefined) {
			latter.get.objectOrder / 2
		} else {
			10
		}
		obj.objectOrder = resultPos
		obj
	}
}
