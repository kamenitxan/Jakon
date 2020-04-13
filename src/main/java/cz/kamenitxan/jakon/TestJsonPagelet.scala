package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.dynamic.{AbstractJsonPagelet, Get, JsonPagelet}
import cz.kamenitxan.jakon.logging.LoggingSetting
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

	@Get(path = "/throw")
	def throwEx(req: Request, res: Response): String = {
		throw new IllegalAccessException()
	}
}
