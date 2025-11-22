package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonDeserializationContext, JsonDeserializer, JsonElement}

import java.lang.reflect.Type
import java.time.ZonedDateTime

/**
 * Created by TPa on 05.06.2022.
 */
class GsonZonedDateTimeDeserializer extends JsonDeserializer[ZonedDateTime] {
	override def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ZonedDateTime = {
		try {
			ZonedDateTime.parse(json.getAsJsonPrimitive.getAsString)
		} catch {
			case _: Exception => null
		}
	}
}
