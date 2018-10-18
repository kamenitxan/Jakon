package cz.kamenitxan.jakon.core.model.converters

import javax.persistence.AttributeConverter

class ScalaMapConverter extends AttributeConverter[Map[String, String], String] {

	override def convertToDatabaseColumn(attribute: Map[String, String]): String = {
		attribute.foldLeft("")((acc, kv) => {
			if (!acc.isEmpty) {
				acc + String.valueOf(Character.toChars(30))
			}
			acc + kv._1 + String.valueOf(Character.toChars(31)) + kv._2
		})
	}

	override def convertToEntityAttribute(dbData: String): Map[String, String] = {
		val split = dbData.split(Character.toChars(30))
		split.map(_.split(Character.toChars(31)))
  		.map{ case Array(k, v) => (k, v)}
  		.toMap
	}
}
