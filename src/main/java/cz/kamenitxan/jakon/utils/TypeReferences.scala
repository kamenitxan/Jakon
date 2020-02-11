package cz.kamenitxan.jakon.utils

import java.time.{LocalDate, LocalDateTime}
import java.util.Date

//noinspection TypeAnnotation
object TypeReferences {

	val STRING = classOf[String]
	val BOOLEAN = classOf[Boolean]
	val INTEGER = classOf[Int]
	val FLOAT = classOf[Float]
	val DOUBLE = classOf[Double]
	val DOUBLE_j = classOf[java.lang.Double]
	val INTEGER_j = classOf[java.lang.Integer]
	val LIST_j = classOf[java.util.List[Any]]
	val MAP = classOf[Map[Any, Any]]
	val DATE = classOf[LocalDate]
	val DATE_o = classOf[Date]
	val DATETIME = classOf[LocalDateTime]
	val SEQ = classOf[Seq[Any]]
}
