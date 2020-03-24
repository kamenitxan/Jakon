package cz.kamenitxan.jakon.webui.controller.impl

import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.core.model.{JakonObject, Ordered}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.utils.{PageContext, Utils}
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{ModelAndView, Request, Response}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object ObjectController {
	val excludedFields = List("url", "sectionName", "objectSettings", "childClass")
	private val numberTypes = classOf[Int] :: classOf[Integer] :: classOf[Double] :: classOf[Float] :: Nil
	private val boolTypes = classOf[Boolean] :: classOf[java.lang.Boolean] :: Nil
	private val UNAUTHORIZED_TMPL = "pages/unauthorized"

	val pageSize = 10

	def getList(req: Request, res: Response): ModelAndView = {
		val objectName = req.params(":name")
		val page = req.queryParams("page")
		val pageNumber = Try(Integer.parseInt(page)).getOrElse(1)
		val filterParams = req.queryMap().toMap.asScala.filter(kv => kv._1.startsWith("filter_") && !kv._2.head.isEmpty).map(kv => kv._1.substring(7) -> kv._2.head)
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName))
		if (objectClass.isDefined) {
			if (!isAuthorized(objectClass.get)) {
				return new Context(Map[String, Any](
					"objectName" -> objectName
				), UNAUTHORIZED_TMPL)
			}
			implicit val conn: Connection = DBHelper.getConnection
			try {
				// pocet objektu
				// language=SQL
				val countSql = s"SELECT count(*) FROM $objectName"
				val count = DBHelper.count(countSql)

				// seznam objektu
				val ocls: Class[JakonObject] = objectClass.get.asInstanceOf[Class[JakonObject]]
				val first = (pageNumber - 1) * pageSize
				val order = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					s"ORDER BY $objectName.objectOrder"
				} else {
					""
				}
				val filterSql = parseFilterParams(filterParams, objectClass.get)
				val joinSql = createSqlJoin(objectClass.get)
				// language=SQL
				val listSql = s"SELECT * FROM JakonObject $joinSql $filterSql $order LIMIT $pageSize OFFSET $first"
				val stmt2 = conn.createStatement()
				val resultList = DBHelper.selectDeep(stmt2, listSql, ocls)
				// TODO: nacist foreign key objekty
				val pageItems: List[JakonObject] = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					Ordered.fetchVisibleOrder(resultList, ocls)
				} else {
					resultList
				}

				val upperClass = {
					val objectSettings = objectClass.get.getDeclaredConstructor().newInstance().objectSettings
					if (objectSettings != null && objectSettings.noParentFieldInList) {
						objectClass.get.getSuperclass
					} else {
						classOf[Object]
					}
				}
				val fields = Utils.getFieldsUpTo(objectClass.get, upperClass).filter(n => !excludedFields.contains(n.getName))
				val fi = FieldConformer.getEmptyFieldInfos(fields)
				new Context(Map[String, Any](
					"objectName" -> objectName,
					"objects" -> pageItems,
					"object" -> ocls.newInstance(), // used for ObjectExtensions
					"pageNumber" -> pageNumber,
					"pageCount" -> Math.max(Math.ceil(count / pageSize.toFloat), 1),
					"objectCount" -> count,
					"fields" -> fi,
					"filterParams" -> filterParams.asJava
				), "objects/list")
			} catch {
				case ex: Throwable =>
					Logger.error("Excetion when getting object list", ex)
					throw ex
			} finally {
				conn.close()
			}
		} else {
			res.status(404)
			new Context(Map[String, Any](), "errors/404")
		}
	}

	private def parseFilterParams(kv: mutable.Map[String, String], objectClass: Class[_]): String = {
		if (kv.isEmpty) {
			return ""
		}
		var notFirst = false
		val sb = new mutable.StringBuilder()
		sb.append("WHERE ")
		for ((fieldName, v) <- kv) {
			if (notFirst) {
				sb.append(" AND ")
			}
			val clr = Utils.getClassByFieldName(objectClass, fieldName)
			sb.append(clr._1.getSimpleName)
			sb.append(".")
			sb.append(fieldName)
			v match {
				case param if param.contains("*") =>
					sb.append(" LIKE \"")
					sb.append(param.replace("*", "%"))
					sb.append("\"")
				case param =>
					sb.append(" = ")
					if (numberTypes.contains(clr._2.getType)) {
						try {
							v.toDouble
							sb.append(param)
						} catch {
							case _: NumberFormatException => sb.append("\"" + v + "\"")
						}

					} else if (boolTypes.contains(clr._2.getType)) {
						try {
							val pbv = v.toBoolean
							if (pbv) sb.append(1) else sb.append(0)
						} catch {
							case _: IllegalArgumentException => sb.append("\"" + v + "\"")
						}

					} else {
						sb.append("\"")
						sb.append(v)
						sb.append("\"")
					}
			}
			notFirst = true
		}
		sb.toString()
	}


	def getItem(req: Request, res: Response): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head

		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), UNAUTHORIZED_TMPL)
		}
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			implicit val conn: Connection = DBHelper.getConnection
			try {
				val joinSql = createSqlJoin(objectClass)
				// language=SQL
				val stmt = conn.prepareStatement(s"SELECT * FROM JakonObject $joinSql WHERE $objectName.id = ?")
				stmt.setInt(1, objectId.get)
				obj = Option(DBHelper.selectSingleDeep(stmt, objectClass)).getOrElse(objectClass.getDeclaredConstructor().newInstance())
				if (obj.getClass.getInterfaces.contains(classOf[Ordered])) {
					obj.asInstanceOf[Ordered].fetchVisibleOrder
				}
			} finally {
				conn.close()
			}
		} else {
			obj = objectClass.getDeclaredConstructor().newInstance()
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
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head

		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), UNAUTHORIZED_TMPL)
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

		val formData = EntityValidator.createFormData(req, objectClass)
		EntityValidator.validate(objectClass.getSimpleName, formData) match {
			case Left(result) =>
				result.foreach(r => PageContext.getInstance().messages += r)
				val redirectUrl = objectId match {
					case Some(id) => s"/admin/object/$objectName/${id}"
					case None => s"/admin/object/create/$objectName"
				}
				return redirect(req, res, redirectUrl)
			case _ =>
		}

		var formOrder = 0
		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRefOpt = Utils.getFieldsUpTo(objectClass, classOf[Object]).find(f => f.getName.startsWith(p))
			if (fieldRefOpt.isDefined) {
				val fieldRef = fieldRefOpt.get
				fieldRef.setAccessible(true)
				val value = req.queryParams(p).trim.conform(fieldRef)
				if (value != null) {
					p match {
						case "visibleOrder" => formOrder = value.asInstanceOf[Int]
						case "objectOrder" =>
						case _ => fieldRef.set(obj, value)
					}
				}
			}
		}

		if (objectId.nonEmpty) {
			if (objectClass.getInterfaces.contains(classOf[Ordered])) {
				obj = DBHelper.withDbConnection(implicit conn => obj.asInstanceOf[Ordered].updateOrder(formOrder))
			}
			obj.update()
		} else {
			obj.create()
		}
		if (req.queryParams("save_and_new").toBoolOrFalse) {
			PageContext.getInstance().addMessage(MessageSeverity.SUCCESS, "NEW_OBJ_CREATED")
			redirect(req, res, "/admin/object/create/" + objectName)
		} else {
			redirect(req, res, "/admin/object/" + objectName)
		}
	}

	def deleteItem(req: Request, res: Response): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt.get
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head
		if (!isAuthorized(objectClass)) {
			return new Context(Map[String, Any](
				"objectName" -> objectName
			), UNAUTHORIZED_TMPL)
		}

		// language=SQL
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
		if (user.get.acl.masterAdmin) {
			true
		} else {
			user.get.acl.allowedControllers.contains(objectClass.getCanonicalName)
		}
	}


	def moveInList(req: Request, res: Response, up: Boolean): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val order = req.queryParams("currentOrder").toOptInt

		val objectClass = DBHelper.getDaoClasses.filter(c => c.getSimpleName.equals(objectName)).head
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "OBJECT_NOT_ORDERED")
			redirect(req, res, "/admin/object/" + objectName)
		}


		val newOrder = if (up) order.get - 1 else order.get + 1

		implicit val conn: Connection = DBHelper.getConnection
		try {

			val ps = conn.prepareStatement("SELECT * FROM " + objectName + " WHERE id = ?")
			ps.setInt(1, objectId.get)
			val obj = DBHelper.selectSingleDeep(ps, objectClass).asInstanceOf[JakonObject with Ordered]
			obj.updateOrder(newOrder)
			obj.update()
		} finally {
			conn.close()
		}

		res.redirect("/admin/object/" + objectName)
		new Context(Map[String, Any](), "objects/list")
	}

	private def redirect(req: Request, res: Response, target: String): Context = {
		req.session().attribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		res.redirect(target)
		null
	}

	@tailrec
	private def createSqlJoin(cls: Class[_], sql: String = ""): String = {
		if (cls != null && cls != classOf[JakonObject]) {
			val objectName = cls.getSimpleName
			val newSql = sql + s"INNER JOIN $objectName ON JakonObject.id = $objectName.id "
			createSqlJoin(cls.getSuperclass, newSql)
		} else {
			sql
		}
	}
}
