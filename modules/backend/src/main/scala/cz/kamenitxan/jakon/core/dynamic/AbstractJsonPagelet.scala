package cz.kamenitxan.jakon.core.dynamic

import com.google.gson.{Gson, GsonBuilder}
import cz.kamenitxan.jakon.core.dynamic.arguments.{CirceJsonParser, ParsedValue}
import cz.kamenitxan.jakon.utils.gson.{GsonMapSerializer, GsonOptionSerializer, GsonSeqSerializer, GsonZonedDateTimeDeserializer, GsonZonedDateTimeSerializer, IntegerTypeAdapter}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import spark.Request

import java.lang.reflect.Field
import java.time.ZonedDateTime

/**
 * Created by TPa on 13.04.2020.
 */
abstract class AbstractJsonPagelet {
	val gson: Gson = new GsonBuilder()
	  .setPrettyPrinting()
		.registerTypeAdapter(classOf[Option[Any]], new GsonOptionSerializer[Any])
		.registerTypeAdapter(classOf[Seq[Any]], new GsonSeqSerializer[Any])
		.registerTypeAdapter(classOf[Map[Any,Any]], new GsonMapSerializer[Any,Any])
		.registerTypeAdapter(classOf[ZonedDateTime], new GsonZonedDateTimeDeserializer)
		.registerTypeAdapter(classOf[ZonedDateTime], new GsonZonedDateTimeSerializer)
		.registerTypeAdapter(classOf[java.lang.Integer], new IntegerTypeAdapter)
	  .create()

	/**
	 * Response will be wrapped into [[cz.kamenitxan.jakon.core.dynamic.entity.AbstractJsonResponse]]
	 */
	val wrapResponse = true
	
	def parseRequestData(req: Request, t: Class[_]): Map[Field, ParsedValue] = {
		CirceJsonParser.parseRequestData(req, t)
	}

	def createDataObject(data: Map[Field, ParsedValue], t: Class[_]): Any = {
		val constructorParams = t.getDeclaredConstructors.head.getParameters.map(p => {
			CirceJsonParser.mapToObject(p, data)
		})

		t.getDeclaredConstructors.head.newInstance(constructorParams: _*)
	}
}
