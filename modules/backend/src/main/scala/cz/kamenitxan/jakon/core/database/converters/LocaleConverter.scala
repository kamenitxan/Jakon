package cz.kamenitxan.jakon.core.database.converters

import java.util.Locale

import cz.kamenitxan.jakon.utils.Utils

/**
 * Created by TPa on 08.12.2020.
 */
class LocaleConverter extends AbstractConverter[Locale] {
	override def convertToDatabaseColumn(attribute: Locale): String = attribute.toString

	override def convertToEntityAttribute(dbData: String): Locale = Utils.stringToLocale(dbData)
}
