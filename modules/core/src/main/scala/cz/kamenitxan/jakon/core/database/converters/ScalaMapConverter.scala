package cz.kamenitxan.jakon.core.database.converters

import cz.kamenitxan.jakon.logging.Logger
import org.apache.commons.lang3.StringUtils

import java.util
import scala.jdk.CollectionConverters.*
import scala.language.postfixOps


class ScalaMapConverter extends AbstractMapConverter[String, String] {

	override def convertToDatabaseColumn(attribute: Map[String, String]): String = {
		gson.toJson(attribute.asJava)
	}

	override def convertToEntityAttribute(dbData: String): Map[String, String] = {
		val resultMap = gson.fromJson(dbData, classOf[util.Map[String, String]])
		if (resultMap != null) {
			resultMap.asScala.toMap
		} else {
			Logger.warn(s"Result is null. dbData: '${StringUtils.abbreviate(dbData, 30)}'")
			null
		}
	}
}
