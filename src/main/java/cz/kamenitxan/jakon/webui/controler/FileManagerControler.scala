package cz.kamenitxan.jakon.webui.controler

import cz.kamenitxan.jakon.webui.Context
import spark.{Request, Response}

object FileManagerControler {

	def getManager(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/fileManager")
	}

	def getManagerFrame(req: Request, res: Response) = {
		new Context(Map[String, Any](), "objects/fileManagerFrame")
	}

	def executeGet(req: Request, res: Response) = {
		val method = req.params(":method")
		val fm = new FileManagerServlet
		fm.init()
		fm.doGet(req.raw(), res.raw())
		res
	}

	def executePost(req: Request, res: Response) = {
		val method = req.params(":method")
		val fm = new FileManagerServlet
		fm.init()
		fm.doPost(req.raw(), res.raw())
		res
	}
}
