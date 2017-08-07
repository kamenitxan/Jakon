package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.utils.Utils._
import cz.kamenitxan.jakon.webui.conform.FieldConformer
import spark.{ModelAndView, Request, Response}
import cz.kamenitxan.jakon.webui.conform.FieldConformer._

import scala.collection.JavaConverters._

/**
  * Created by TPa on 08.09.16.
  */
object ObjectControler {
	val excludedFields = List("url", "sectionName", "objectSettings")


	def getList(req: Request, res: Response): ModelAndView = {
		val objectName = req.params(":name")
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		if (objectClass != null) {
			val objects = DBHelper.getDao(objectClass).queryForAll()
			val fields = objectClass.getDeclaredFields.map(f => f.getName).filter(n => !excludedFields.contains(n)).toList.asJava
			new Context(Map[String, Any](
				"objectName" -> objectName,
				"objects" -> objects,
				"fields" -> fields
			), "objects/list")
		} else {
			new Context(Map[String, Any](), "objects/list")
		}
	}


	def getItem(req: Request, res: Response) = {
		val objectName = req.params(":name")
		val objectId = req.params(":id").toOptInt
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		var obj: JakonObject = null
		if (objectId.nonEmpty) {
			obj = Option(DBHelper.getDao(objectClass).queryForId(objectId.get)).getOrElse(objectClass.newInstance())
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
			obj = Option(DBHelper.getDao(objectClass).queryForId(objectId.get)).getOrElse(objectClass.newInstance())
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
		DBHelper.getDao(objectClass).deleteById(objectId)
		res.redirect("/admin/object/" + objectName)
		new Context(Map[String, Any](), "objects/list")
	}
}
