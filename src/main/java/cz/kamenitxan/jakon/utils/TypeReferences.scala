package cz.kamenitxan.jakon.utils

import java.time.LocalDateTime
import java.util.Date

object TypeReferences {

	val STRING = classOf[String]
	val BOOLEAN = classOf[Boolean]
	val INTEGER = classOf[Int]
	val DOUBLE = classOf[Double]
	val DOUBLE_j = classOf[java.lang.Double]
	val LONG = classOf[Long]
	val INTEGER_j = classOf[java.lang.Integer]
	val LIST_j = classOf[java.util.List[Any]]
	val MAP = classOf[Map[Any, Any]]
	val DATE = classOf[Date]
	val DATETIME = classOf[LocalDateTime]
	val SEQ = classOf[Seq[Any]]
}
