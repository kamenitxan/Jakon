package cz.kamenitxan.jakon.core.database.converters

import java.util

import scala.collection.JavaConverters._
import scala.language.postfixOps


class ScalaMapConverter extends AbstractMapConverter[String, String] {

	override def convertToDatabaseColumn(attribute: Map[String, String]): String = {
		gson.toJson(attribute.asJava)
	}

	override def convertToEntityAttribute(dbData: String): Map[String, String] = {
		gson.fromJson(dbData, classOf[util.Map[String, String]]).asScala.toMap
	}
}
