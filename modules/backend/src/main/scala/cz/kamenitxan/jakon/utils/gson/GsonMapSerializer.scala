package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonElement, JsonSerializationContext, JsonSerializer}

import java.lang.reflect.Type
import scala.jdk.CollectionConverters.*

class GsonMapSerializer[K,V] extends JsonSerializer[Map[K,V]] {

	def serialize(src: Map[K,V], typeOfSrc: Type , context: JsonSerializationContext): JsonElement = context.serialize(src.asJava)
}
