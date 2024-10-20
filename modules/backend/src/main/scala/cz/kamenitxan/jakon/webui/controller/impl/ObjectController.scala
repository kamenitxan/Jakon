package cz.kamenitxan.jakon.webui.controller.impl

import cz.kamenitxan.jakon.core.configuration.Settings
import cz.kamenitxan.jakon.core.database.{DBHelper, I18n}
import cz.kamenitxan.jakon.core.model.{I18nData, JakonObject, Ordered}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils.*
import cz.kamenitxan.jakon.utils.{ContextExtension, PageContext, SqlGen, Utils}
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.ModelAndView
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import cz.kamenitxan.jakon.webui.conform.FieldConformer.*
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context
import org.sqlite.{SQLiteErrorCode, SQLiteException}

import java.lang.reflect.Field
import java.sql.Connection
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
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

	def getList(ctx: Context): ModelAndView = {
		val objectName = ctx.pathParam("name")
		val page = ctx.queryParam("page")
		val pageNumber = Try(Integer.parseInt(page)).getOrElse(1)
		val filterParams = ctx.queryParamMap().asScala.filter(kv => kv._1.startsWith("filter_") && kv._2.asScala.head.nonEmpty).map(kv => kv._1.substring(7) -> kv._2.asScala.head)
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName))
		if (objectClass.isDefined) {
			if (!isAuthorized(objectClass.get)) {
				return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
					"objectName" -> objectName
				), UNAUTHORIZED_TMPL)
			}
			val objectSettings = objectClass.get.getDeclaredConstructor().newInstance().objectSettings
			implicit val conn: Connection = DBHelper.getConnection
			try {
				val filterSql = {
					val fp = SqlGen.parseFilterParams(filterParams, objectClass.get)
					val of = if (objectSettings != null) objectSettings.customFilter else ""
					if(fp.nonEmpty && of.nonEmpty) fp + " AND " + of
					else if (fp.nonEmpty && of.isEmpty) fp
					else if (fp.isEmpty && of.nonEmpty) "WHERE " + of
					else ""

				}
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
				new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
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
			ctx.status(404)
			new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), "errors/404")
		}
	}

	def getItem(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		val objectName = ctx.pathParam("name")
		val objectId = ctx.`with`(classOf[ContextExtension]).pathParamOpt("id").map(_.toInt)
		val filteredClasses = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName))
		if (filteredClasses.isEmpty) {
			ctx.status(404)
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), "errors/404")
		}
		implicit val objectClass: Class[_ <: JakonObject] = filteredClasses.head

		if (!isAuthorized(objectClass)) {
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
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
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
			"objectName" -> objectName,
			"object" -> obj,
			"id" -> obj.id,
			"fields" -> f,
			"page" -> ctx.queryParam("page")
		), "objects/single")
	}

	def updateItem(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		val params = ctx.queryParamMap().asScala.keySet.toSet
		val objectName = ctx.pathParam("name")
		val objectId = ctx.`with`(classOf[ContextExtension]).pathParamOpt("id").map(_.toInt)
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head

		if (!isAuthorized(objectClass)) {
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
				"objectName" -> objectName
			), UNAUTHORIZED_TMPL)
		}
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			implicit val conn: Connection = DBHelper.getConnection
			val stmt = conn.prepareStatement("SELECT id FROM JakonObject WHERE id = ?")
			stmt.setInt(1, objectId.get)
			obj = Option(DBHelper.selectSingle(stmt, objectClass).entity).getOrElse(objectClass.getDeclaredConstructor().newInstance())
			conn.close()
		} else {
			obj = objectClass.getDeclaredConstructor().newInstance()
		}

		val formData: Map[Field, String] = if (objectId.nonEmpty) {
			EntityValidator.createFormData(ctx, objectClass) + (Utils.getFields(classOf[JakonObject]).find(_.getName == "id").get -> objectId.get.toString)
		} else {
			EntityValidator.createFormData(ctx, objectClass)
		}
		EntityValidator.validate(objectClass.getSimpleName, formData) match {
			case Left(result) =>
				result.foreach(r => PageContext.getInstance().messages += r)
				if (result.exists(r => r._severity == MessageSeverity.ERROR)) {
					val redirectUrl = objectId match {
						case Some(id) => s"/admin/object/$objectName/$id"
						case None => s"/admin/object/create/$objectName"
					}
					return redirect(ctx, redirectUrl)
				}
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
				val value = ctx.queryParams(p).asScala.map(_.trim).mkString("\r\n").conform(fieldRef)
				if (value != null) {
					p match {
						case "visibleOrder" => formOrder = value.asInstanceOf[Int]
						case "objectOrder" =>
						case _ => fieldRef.set(obj, value)
					}
				}
			}
		}


		try {
			if (objectId.nonEmpty) {
				if (objectClass.getInterfaces.contains(classOf[Ordered])) {
					obj = DBHelper.withDbConnection(implicit conn => obj.asInstanceOf[Ordered].updateOrder(formOrder))
				}
				obj.update()
			} else {
				obj.create()
			}
		} catch {
			case ex: SQLiteException =>
				if (ex.getResultCode == SQLiteErrorCode.SQLITE_CONSTRAINT && ex.getMessage.contains("UNIQUE")) {
					// TODO: mysql
					// TODO: better msg
					PageContext.getInstance().addMessage(MessageSeverity.ERROR, "NEW_OBJ_UNIQUE_FAIL")
					redirect(ctx, "/admin/object/create/" + objectName)
				} else {
					throw ex
				}
		}

		val i18nFieldOpt = fields.find(_.getAnnotation(classOf[I18n]) != null)
		if (i18nFieldOpt.nonEmpty) {
			createI18nData(obj.id, i18nFieldOpt.get, ctx, params)
		}

		if (ctx.queryParam("save_and_new").toBoolOrFalse) {
			PageContext.getInstance().addMessage(MessageSeverity.SUCCESS, "NEW_OBJ_CREATED")
			redirect(ctx, "/admin/object/create/" + objectName)
		} else {
			val target = ObjectPath + objectName + s"?page=${ctx.queryParam("admin_page")}"
			redirect(ctx, target)
		}
	}

	def deleteItem(ctx: Context): cz.kamenitxan.jakon.webui.Context = {
		val objectName = ctx.pathParam("name")
		val objectId = ctx.pathParam("id").toOptInt.get
		val objectClass = DBHelper.getDaoClasses.find(c => c.getSimpleName.equals(objectName)).head
		if (!isAuthorized(objectClass)) {
			return new cz.kamenitxan.jakon.webui.Context(Map[String, Any](
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

		ctx.redirect(ObjectPath + objectName)
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), ListTmpl)
	}

	private def isAuthorized(objectClass: Class[_]): Boolean = {
		val user = PageContext.getInstance().getLoggedUser
		if (user.get.acl.masterAdmin) {
			true
		} else {
			user.get.acl.allowedControllers.contains(objectClass.getCanonicalName)
		}
	}


	def moveInList(ctx: Context, up: Boolean): cz.kamenitxan.jakon.webui.Context = {
		val objectName = ctx.pathParam("name")
		val objectId = ctx.pathParam("id").toOptInt
		val order = ctx.queryParam("currentOrder").toOptInt

		implicit val objectClass: Class[_ <: JakonObject] = DBHelper.getDaoClasses.filter(c => c.getSimpleName.equals(objectName)).head
		if (!objectClass.getInterfaces.contains(classOf[Ordered])) {
			PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "OBJECT_NOT_ORDERED")
			redirect(ctx, ObjectPath + objectName)
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

		ctx.redirect(ObjectPath + objectName)
		new cz.kamenitxan.jakon.webui.Context(Map[String, Any](), ListTmpl)
	}

	private def redirect(ctx: Context, target: String): cz.kamenitxan.jakon.webui.Context = {
		ctx.sessionAttribute(PageContext.MESSAGES_KEY, PageContext.getInstance().messages)
		ctx.redirect(target)
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

	private def createI18nData(id: Integer, f: Field, ctx: Context, params: Set[String]): Any = {
		implicit val cls: Class[_] = f.getDeclaredAnnotation(classOf[I18n]).genericClass()
		val fields = Utils.getFieldsUpTo(cls, classOf[Object])
		Settings.getSupportedLocales.foreach(l => {
			val isUpdate = DBHelper.withDbConnection(implicit conn => {
				val sql = s"SELECT count(*) FROM ${cls.getSimpleName} WHERE id = ? AND locale = ?"
				val stmt = conn.prepareStatement(sql)
				stmt.setInt(1, id)
				stmt.setString(2, l.toString)
				DBHelper.count(stmt) > 0
			})
			val i18nData = cls.getDeclaredConstructor().newInstance().asInstanceOf[I18nData]
			i18nData.id = id
			i18nData.locale = l
			params.filter(_.endsWith(l.toString)).foreach(p => {
				val fieldName = p.replace("-" + l.toString, "")
				val fieldRefOpt = fields.find(f => f.getName == fieldName)
				if (fieldRefOpt.isDefined) {
					val fieldRef = fieldRefOpt.get
					fieldRef.setAccessible(true)
					val value = ctx.queryParam(p).conform(fieldRef)
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
