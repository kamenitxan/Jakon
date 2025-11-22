package jakontest.core.pagelet.entity

import cz.kamenitxan.jakon.core.dynamic.entity.{AbstractJsonResponse, ResponseStatus}

import java.time.{LocalDateTime, ZonedDateTime}


case class SingleTestResponse[T](single: T) extends AbstractJsonResponse[T](ResponseStatus.success, single)


/**
 * Created by Kamenitxan on 13.07.2024
 */
case class TypedTestResponseData(
															msg: String,
															num: Int,
															seqStr: Seq[String],
															seqInt: Seq[Int],
															optStr: Option[String],
															optStrEmpty: Option[String],
															optInt: Option[Int],
															localDateTime: LocalDateTime,
															zonedDateTimeData: ZonedDateTime,
															integer: Integer,
															integerNull: Integer,
															map: Map[String, String],
														)
