package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}

/**
  * Created by TPa on 08.09.16.
  */
object ObjectControler {

	def getList(req: Request, res: Response) = {
		new Context(Map[String, Any](
			"objectName" -> req.params(":name")
		), "objects/list")
	}

	def getItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}

	def createItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}

	def updateItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}

	def deleteItem(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/list")
	}
}
