package jakontest.test

import com.google.gson.*

import scala.language.implicitConversions

/**
 * Created by TPa on 15.04.2020.
 */
object JsonHelper {
	def JSON(props: (String, JsonElement)*): JsonObject = {
		props.foldLeft(new JsonObject)((json, pair) => {
			json.add(pair._1, pair._2)
			json
		})
	}

	implicit def string2json(s: String): JsonPrimitive = new JsonPrimitive(s)
	implicit def number2json(n: Number): JsonPrimitive = new JsonPrimitive(n)
	implicit def boolean2json(b: Boolean): JsonPrimitive = new JsonPrimitive(b)
	implicit def list2array(lis: List[Any]): JsonArray =
		lis.foldLeft(new JsonArray)((array, item) => {
			array.add(convertToJson(item))
			array
		})

	def convertToJson(obj: Any): JsonElement = obj match {
		case e: JsonElement => e
		case lis: List[Any] => list2array(lis)
		case s: String => new JsonPrimitive(s)
		case n: Number => new JsonPrimitive(n)
		case b: Boolean => new JsonPrimitive(b)
		case null => JsonNull.INSTANCE
		case _ => throw new RuntimeException
	}
}
