package cz.kamenitxan.jakon.core.database.converters

import java.util

class StringListConverter extends AbstractListConverter[String] {

	override def convertToDatabaseColumn(attribute: util.List[String]): String = {
		gson.toJson(attribute)
	}

	override def convertToEntityAttribute(dbData: String): util.List[String] = {
		gson.fromJson(dbData, classOf[util.List[String]])
	}
}
