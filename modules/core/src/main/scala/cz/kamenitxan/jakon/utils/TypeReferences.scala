package cz.kamenitxan.jakon.utils

import io.javalin.http.Context

import java.sql.Connection
import java.time.{LocalDate, LocalDateTime, LocalTime, ZonedDateTime}
import java.util.{Date, Locale}

//noinspection TypeAnnotation
object TypeReferences {

	val STRING = classOf[String]
	val BOOLEAN = classOf[Boolean]
	val BOOLEAN_j = classOf[java.lang.Boolean]
	val INTEGER = classOf[Int]
	val FLOAT = classOf[Float]
	val DOUBLE = classOf[Double]
	val DOUBLE_j = classOf[java.lang.Double]
	val INTEGER_j = classOf[java.lang.Integer]
	val BIG_DECIMAL_j = classOf[java.math.BigDecimal]
	val LIST_j = classOf[java.util.List[Any]]
	val ARRAY_LIST_j = classOf[java.util.ArrayList[Any]]
	val MAP = classOf[Map[Any, Any]]
	val DATE = classOf[LocalDate]
	val DATE_o = classOf[Date]
	val SQL_DATE = classOf[java.sql.Date]
	val DATETIME = classOf[LocalDateTime]
	val ZONED_DATETIME = classOf[ZonedDateTime]
	val TIME = classOf[LocalTime]
	val SEQ = classOf[Seq[Any]]
	val LOCALE = classOf[Locale]

	val CONTEXT_CLS = classOf[Context]
	val CONNECTION_CLS = classOf[Connection]
}
