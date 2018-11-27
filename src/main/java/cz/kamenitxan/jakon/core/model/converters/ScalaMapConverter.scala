package cz.kamenitxan.jakon.core.model.converters

import com.google.gson.Gson
import javax.persistence.AttributeConverter
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.language.postfixOps


class ScalaMapConverter extends AttributeConverter[Map[String, String], String] {
	private val gson = new Gson()

	override def convertToDatabaseColumn(attribute: Map[String, String]): String = {
		gson.toJson(attribute)
	}

	override def convertToEntityAttribute(dbData: String): Map[String, String] = {
		gson.fromJson(dbData, classOf[util.Map[String, String]]).asScala.toMap
	}
}
