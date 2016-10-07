package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.core.model.Dao.DBHelper
import cz.kamenitxan.jakon.webui.Context
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
		val objectId = Integer.valueOf(req.params(":id"))
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		val obj = DBHelper.getDao(objectClass).queryForId(objectId)
		val fields = objectClass.getDeclaredFields.map(f => f.getName -> f.getType).filter(n => !excludedFields.contains(n._2.getSimpleName)).toList.asJava
		new Context(Map[String, Any](
			"objectName" -> objectName,
			"object" -> obj,
			"fields" -> fields
		), "objects/single")
	}

	def createItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}

	def updateItem(req: Request, res: Response) = {
		val params = req.requestMethod()
		val objectName = req.params(":name")
		val objectClass = DBHelper.getDaoClasses.filter(c => c.getName.contains(objectName)).head
		val fields = objectClass.getDeclaredFields.map(f => f.getName).filter(n => !excludedFields.contains(n)).toList.asJava
		new Context(Map[String, Any](), "objects/list")
	}

	def deleteItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}
}
