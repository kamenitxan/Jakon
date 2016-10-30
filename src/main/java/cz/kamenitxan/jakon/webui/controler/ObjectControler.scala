package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.core.model.JakonObject
import cz.kamenitxan.jakon.webui.Context
import cz.kamenitxan.jakon.utils.Utils._
import spark.{ModelAndView, Request, Response}

import scala.collection.JavaConverters._

/**
  * Created by TPa on 08.09.16.
  */
object ObjectControler {
	val excludedFields = List("url", "sectionName", "ObjectSettings")

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
		val fields = objectClass.getDeclaredFields.map(f => f.getName -> f.getType).filter(n => !excludedFields.contains(n._2.getSimpleName)).toList.asJava
		new Context(Map[String, Any](
			"objectName" -> objectName,
			"object" -> obj,
			"id" -> obj.id,
			"fields" -> fields
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

		for (p <- params) {
			val fieldRef = objectClass.getDeclaredField(p)
			fieldRef.setAccessible(true)
			fieldRef.set(obj, req.queryParams(p))
		}
		DBHelper.getDao(objectClass).createOrUpdate(obj)
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
