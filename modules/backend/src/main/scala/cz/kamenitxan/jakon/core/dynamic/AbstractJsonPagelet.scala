package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.{Gson, GsonBuilder}
import spark.Request

/**
 * Created by TPa on 13.04.2020.
 */
abstract class AbstractJsonPagelet {
	val gson: Gson = new GsonBuilder()
	  .setPrettyPrinting()
	  .create()

	/**
	 * Response will be wrapped into [[cz.kamenitxan.jakon.core.dynamic.entity.AbstractJsonResponse]]
	 */
	val wrapResponse = true
	
	def parseRequestData(req: Request, t: Class[_]) = gson.fromJson(req.body(), t)
}
