package cz.kamenitxan.jakon.utils

import java.sql.Connection
import java.time.{LocalDate, LocalDateTime}
import java.util.{Date, Locale}
import spark.{Request, Response}

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
	val ARRAY_LIST_j = classOf[java.util.ArrayList[Any]]
	val MAP = classOf[Map[Any, Any]]
	val DATE = classOf[LocalDate]
	val DATE_o = classOf[Date]
	val SQL_DATE = classOf[java.sql.Date]
	val DATETIME = classOf[LocalDateTime]
	val SEQ = classOf[Seq[Any]]
	val LOCALE = classOf[Locale]

	val REQUEST_CLS = classOf[Request]
	val RESPONSE_CLS = classOf[Response]
	val CONNECTION_CLS = classOf[Connection]
}
