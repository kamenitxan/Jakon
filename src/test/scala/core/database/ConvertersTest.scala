package core.database

import java.util

import cz.kamenitxan.jakon.core.database.converters.{LocaleConverter, StringListConverter}
import cz.kamenitxan.jakon.utils.Utils
import org.scalatest.funsuite.AnyFunSuite

class ConvertersTest extends AnyFunSuite {

	test("LocalConverter") {
		val c = new LocaleConverter
		val locale = Utils.stringToLocale("cs_CZ")

		val sl = c.convertToDatabaseColumn(locale)
		val ll = c.convertToEntityAttribute(sl)

		assert(locale == ll)
	}

	test("StringListConverter") {
		val c = new StringListConverter
		val list = new util.ArrayList[String]()
		list.add("1")
		list.add("2")

		val sl = c.convertToDatabaseColumn(list)
		val ll = c.convertToEntityAttribute(sl)

		assert(list == ll)
	}
}
