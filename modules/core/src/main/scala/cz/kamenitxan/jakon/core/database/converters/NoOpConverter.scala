package cz.kamenitxan.jakon.core.database.converters

/**
  * Created by TPa on 06/10/2021.
  */
class NoOpConverter extends AbstractConverter[Any] {
	override def convertToDatabaseColumn(attribute: Any): String = {
		throw new UnsupportedOperationException
	}

	override def convertToEntityAttribute(dbData: String): Any = {
		null
	}
}
