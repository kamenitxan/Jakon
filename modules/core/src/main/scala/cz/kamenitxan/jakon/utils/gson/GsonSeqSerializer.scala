package cz.kamenitxan.jakon.utils.gson

import com.google.gson.{JsonElement, JsonSerializationContext, JsonSerializer}

import java.lang.reflect.Type
import scala.jdk.CollectionConverters.*

class GsonSeqSerializer[T] extends JsonSerializer[Seq[T]] {

	def serialize(src: Seq[T], typeOfSrc: Type , context: JsonSerializationContext): JsonElement = context.serialize(src.asJava)
}
