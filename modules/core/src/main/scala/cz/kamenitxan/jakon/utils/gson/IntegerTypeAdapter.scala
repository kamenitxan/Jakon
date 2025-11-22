package cz.kamenitxan.jakon.utils.gson

import com.google.gson.TypeAdapter
import com.google.gson.stream.{JsonReader, JsonToken, JsonWriter}

/**
 * Created by TPa on 10.08.2022.
 */
class IntegerTypeAdapter extends TypeAdapter[java.lang.Integer]{


	override def write(writer: JsonWriter, value: java.lang.Integer): Unit = {
		writer.value(value)
	}

	override def read(reader: JsonReader): java.lang.Integer = {
		if (reader.peek eq JsonToken.NULL) {
			reader.nextNull()
			return null
		}
		val stringValue = reader.nextString
		try {
			val value = stringValue.toInt
			value
		} catch {
			case _: NumberFormatException => null
		}
	}
}
