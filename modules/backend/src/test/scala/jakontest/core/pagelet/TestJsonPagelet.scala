package jakontest.core.pagelet

import jakontest.core.pagelet.entity.{OldTestJsonPageletData, SingleTestResponse, TestJsonPageletData, TypedTestResponseData}
import cz.kamenitxan.jakon.core.dynamic.entity.{AbstractJsonResponse, JsonFailResponse}
import cz.kamenitxan.jakon.core.dynamic.{AbstractJsonPagelet, Get, JsonPagelet, Post}
import cz.kamenitxan.jakon.utils.Utils.*
import spark.{Request, Response}

import java.sql.Connection
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}

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
	def getResponse(req: Request, res: Response): JsonFailResponse[String] = {
		new JsonFailResponse("some_message")
	}

	@Get(path = "/throw")
	def throwEx(req: Request, res: Response): String = {
		throw new IllegalAccessException()
	}

	@Get(path = "/withDataAndConnection")
	def withDataAndConnection(req: Request, res: Response, data: OldTestJsonPageletData, conn: Connection): String = {
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
		if data.msg.isNullOrEmpty then throw IllegalArgumentException("msg is empty")
		data.msg
	}

	@Post(path = "/postValidate")
	def postValidate(req: Request, res: Response, data: TestJsonPageletData): String = {
		"validation_ok"
	}

	@Get(path = "/getTyped")
	def getTyped(req: Request, res: Response): AbstractJsonResponse[_ <: Any] = {
		val data = TypedTestResponseData(
			"msg",
			1,
			Seq("a", "b"),
			Seq(18, 19),
			Some("optStr"),
			Option.empty,
			Some(75),
			LocalDateTime.of(2024, 7, 13, 0, 0),
			ZonedDateTime.of(2024, 7, 13, 0, 0, 0, 0, ZoneId.of("UTC")),
			1,
			null,
			Map("key" -> "value")
		)
		SingleTestResponse(data)
	}
}
