package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonElement, JsonSerializationContext, JsonSerializer}

import java.lang.reflect.Type
import java.time.LocalDateTime

class GsonLocalDateTimeSerializer extends JsonSerializer[LocalDateTime] {

	def serialize(src: LocalDateTime, typeOfSrc: Type , context: JsonSerializationContext): JsonElement = context.serialize(src.toString)
}
