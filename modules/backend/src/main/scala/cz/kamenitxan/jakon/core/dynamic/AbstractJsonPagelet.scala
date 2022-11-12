package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.{Gson, GsonBuilder}
import cz.kamenitxan.jakon.Circe.{ParsedValue, mapToValue}
import cz.kamenitxan.jakon.Circe
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import spark.Request

import java.lang.reflect.Field

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
	
	def parseRequestData(req: Request, t: Class[_]): Map[Field, ParsedValue] = {
		val res = parser.parse(req.body()).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor

		val fields = t.getDeclaredFields
		fields.map(f => Circe.mapToString(hc, f)).toMap[Field, ParsedValue]
	}
	def createDataObject(data: Map[Field, ParsedValue], t: Class[_]): Any = {
		val constructorParams = t.getDeclaredConstructors.head.getParameters.map(p => {
			Circe.mapToValue(p, data)
		})

		t.getDeclaredConstructors.head.newInstance(constructorParams: _*)
	}
}
