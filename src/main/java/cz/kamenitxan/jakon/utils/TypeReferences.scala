package cz.kamenitxan.jakon.utils

import java.time.LocalDateTime
import java.util.Date

object TypeReferences {

	val S = classOf[String]
	val B = classOf[Boolean]
	val I = classOf[Int]
	val D = classOf[Double]
	val D_j = classOf[java.lang.Double]
	val I_j = classOf[java.lang.Integer]
	val LIST_j = classOf[java.util.List[Any]]
	val MAP = classOf[Map[Any, Any]]
	val DATE = classOf[Date]
	val DATETIME = classOf[LocalDateTime]
}
