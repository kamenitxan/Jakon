package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonElement, JsonSerializationContext, JsonSerializer}

import java.lang.reflect.Type
import java.time.ZonedDateTime

class GsonZonedDateTimeSerializer extends JsonSerializer[ZonedDateTime] {

	def serialize(src: ZonedDateTime, typeOfSrc: Type , context: JsonSerializationContext): JsonElement = context.serialize(src.toOffsetDateTime.toString)
}
