package jakon.pagelet

import java.sql.Connection

import cz.kamenitxan.jakon.core.dynamic.entity.{JsonErrorResponse, JsonFailResponse, ResponseStatus}
import cz.kamenitxan.jakon.core.dynamic.{AbstractJsonPagelet, Get, JsonPagelet, Post}
import jakon.pagelet.entity.{GetResponse, TestJsonPageletData}
import spark.{Request, Response}

/**
 * Created by TPa on 13.04.2020.
 */
@JsonPagelet(path = "jsonPagelet")
class TestJsonPagelet extends AbstractJsonPagelet {

	@Get(path = "/get")
	def get(req: Request, res: Response): String = {
		"string"
	}

	@Get(path = "/getResponse")
	def getResponse(req: Request, res: Response): JsonFailResponse = {
		new JsonFailResponse("some_message")
	}

	@Get(path = "/throw")
	def throwEx(req: Request, res: Response): String = {
		throw new IllegalAccessException()
	}

	@Get(path = "/withDataAndConnection")
	def withDataAndConnection(req: Request, res: Response, data: TestJsonPageletData, conn: Connection): String = {
		data.msg
	}

	@Post(path = "/post")
	def post(req: Request, res: Response): String = {
		"string"
	}

	@Post(path = "/postNoValidation", validate = false)
	def postNoValidation(req: Request, res: Response): String = {
		"string"
	}

	@Post(path = "/postThrow")
	def postThrowEx(req: Request, res: Response): String = {
		throw new IllegalAccessException()
	}

	@Post(path = "/postWithDataAndConnection")
	def postWithDataAndConnection(req: Request, res: Response, data: TestJsonPageletData, conn: Connection): String = {
		data.msg
	}

	@Post(path = "/postValidate")
	def postValidate(req: Request, res: Response, data: TestJsonPageletData): String = {
		"validation_ok"
	}
}
