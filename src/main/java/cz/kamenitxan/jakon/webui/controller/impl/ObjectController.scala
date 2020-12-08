package cz.kamenitxan.jakon.webui.controller.impl

import java.lang.reflect.Field
import java.sql.Connection

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.{DBHelper, I18n}
import cz.kamenitxan.jakon.core.model.{I18nData, JakonObject, Ordered}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.utils.{PageContext, SqlGen, Utils}
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer._
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import spark.{ModelAndView, Request, Response}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  * Created by TPa on 08.09.16.
  */
object ObjectController {
	val excludedFields = Seq("url", "sectionName", "objectSettings", "childClass")
	private val UNAUTHORIZED_TMPL = "pages/unauthorized"
	private val ListTmpl = "objects/list"
	private val ObjectPath = "/admin/object/"

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
			val objectSettings = objectClass.get.getDeclaredConstructor().newInstance().objectSettings
			implicit val conn: Connection = DBHelper.getConnection
			try {
				val filterSql = SqlGen.parseFilterParams(filterParams, objectClass.get)
				val joinSql = createSqlJoin(objectClass.get)
				// pocet objektu
				// language=SQL
				val countSql = s"SELECT count(*) FROM JakonObject $joinSql $filterSql"
				val count = DBHelper.count(countSql)

				// seznam objektu
				implicit val ocls: Class[JakonObject] = objectClass.get.asInstanceOf[Class[JakonObject]]
				val first = (pageNumber - 1) * pageSize
				val orderDirection = if (objectSettings != null) objectSettings.sortDirection else "ASC"
				val order = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					s"ORDER BY $objectName.objectOrder $orderDirection"
				} else {
					s"ORDER BY id $orderDirection"
				}

				// language=SQL
				val listSql = s"SELECT * FROM JakonObject $joinSql $filterSql $order LIMIT $pageSize OFFSET $first"
				val stmt2 = conn.createStatement()
				val resultList = DBHelper.selectDeep(stmt2, listSql)
				// TODO: nacist foreign key objekty
				val pageItems: List[JakonObject] = if (ocls.getInterfaces.contains(classOf[Ordered])) {
					Ordered.fetchVisibleOrder(resultList, ocls)
				} else {
					resultList
				}

				import scala.language.existentials
				val upperClass = {
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
					"object" -> ocls.getDeclaredConstructor().newInstance(), // used for ObjectExtensions
					"pageNumber" -> pageNumber,
					"pageCount" -> Math.max(Math.ceil(count / pageSize.toFloat), 1),
					"objectCount" -> count,
					"fields" -> fi,
					"filterParams" -> filterParams.asJava
				), ListTmpl)
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

	def getItem(req: Request, res: Response): Context = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		implicit val objectClass: Class[_ <: JakonObject] = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head

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
				obj = Option(DBHelper.selectSingleDeep(stmt)).getOrElse(objectClass.getDeclaredConstructor().newInstance())
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
			"fields" -> f,
			"page" -> req.queryParams("page")
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
			obj = Option(DBHelper.selectSingle(stmt, objectClass).entity).getOrElse(objectClass.getDeclaredConstructor().newInstance())
			conn.close()
		} else {
			obj = objectClass.getDeclaredConstructor().newInstance()
		}

		val formData = EntityValidator.createFormData(req, objectClass)
		EntityValidator.validate(objectClass.getSimpleName, formData) match {
			case Left(result) =>
				result.foreach(r => PageContext.getInstance().messages += r)
				val redirectUrl = objectId match {
					case Some(id) => s"/admin/object/$objectName/$id"
					case None => s"/admin/object/create/$objectName"
				}
				return redirect(req, res, redirectUrl)
			case _ =>
		}

		var formOrder = 0
		val fields = Utils.getFieldsUpTo(objectClass, classOf[Object])
		for (p <- params.filter(p => !p.equals("id"))) {
			//TODO optimalizovat
			val fieldRefOpt = fields.find(f => f.getName.startsWith(p))
			if (fieldRefOpt.isDefined) {
				val fieldRef = fieldRefOpt.get
				fieldRef.setAccessible(true)
				val value = req.queryParamsValues(p).map(_.trim).mkString("\r\n").conform(fieldRef)
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
		val i18nFieldOpt = fields.find(_.getAnnotation(classOf[I18n]) != null)
		if (i18nFieldOpt.nonEmpty) {
			createI18nData(obj.id, i18nFieldOpt.get, req, params)
		}

		if (req.queryParams("save_and_new").toBoolOrFalse) {
			PageContext.getInstance().addMessage(MessageSeverity.SUCCESS, "NEW_OBJ_CREATED")
			redirect(req, res, "/admin/object/create/" + objectName)
		} else {
			val target = ObjectPath + objectName + s"?page=${req.queryParams("admin_page")}"
			redirect(req, res, target)
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

		res.redirect(ObjectPath + objectName)
		new Context(Map[String, Any](), ListTmpl)
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

		implicit val objectClass: Class[_ <: JakonObject] = DBHelper.getDaoClasses.filter(c => c.getSimpleName.equals(objectName)).head
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "OBJECT_NOT_ORDERED")
			redirect(req, res, ObjectPath + objectName)
		}


		val newOrder = if (up) order.get - 1 else order.get + 1

		implicit val conn: Connection = DBHelper.getConnection
		try {

			val ps = conn.prepareStatement("SELECT * FROM " + objectName + " WHERE id = ?")
			ps.setInt(1, objectId.get)
			val obj = DBHelper.selectSingleDeep(ps).asInstanceOf[JakonObject with Ordered]
			obj.updateOrder(newOrder)
			obj.update()
		} finally {
			conn.close()
		}

		res.redirect(ObjectPath + objectName)
		new Context(Map[String, Any](), ListTmpl)
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

	private def createI18nData(id: Integer, f: Field, req: Request, params: mutable.Set[String]): Any = {
		implicit val cls: Class[_] = f.getCollectionGenericTypeClass
		val fields = Utils.getFieldsUpTo(cls, classOf[Object])
		Settings.getSupportedLocales.foreach(l => {
			val isUpdate = DBHelper.withDbConnection(implicit conn => {
				val sql = s"SELECT count(*) FROM ${cls.getSimpleName} WHERE id = ? AND locale = ?"
				val stmt = conn.prepareStatement(sql)
				stmt.setInt(1, id)
				stmt.setString(2, l.toString)
				DBHelper.count(stmt) > 0
			})
			val i18nData = cls.newInstance().asInstanceOf[I18nData]
			i18nData.id = id
			i18nData.locale = l
			params.filter(_.endsWith(l.toString)).foreach(p => {
				val fieldName = p.replace("-" + l.toString, "")
				val fieldRefOpt = fields.find(f => f.getName == (fieldName))
				if (fieldRefOpt.isDefined) {
					val fieldRef = fieldRefOpt.get
					fieldRef.setAccessible(true)
					val value = req.queryParams(p).conform(fieldRef)
					if (value != null) {
						fieldRef.set(i18nData, value)
					}
				}
			})
			if (isUpdate) {
				i18nData.update()
			} else {
				i18nData.create()
			}
		})
	}
}
