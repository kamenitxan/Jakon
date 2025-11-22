package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonElement, JsonSerializationContext, JsonSerializer}

import java.lang.reflect.Type

class GsonOptionSerializer[T] extends JsonSerializer[Option[T]] {

	def serialize(src: Option[T], typeOfSrc: Type , context: JsonSerializationContext): JsonElement = context.serialize(src.orNull)
}
