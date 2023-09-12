package cz.kamenitxan.jakon.core.dynamic.arguments

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.utils.Utils.*
import spark.Request

import java.lang.reflect.{Field, Parameter, ParameterizedType}
import java.time.{LocalDate, ZonedDateTime}

/**
 * Parses pagelet request data into Map[Field, ParsedValue]. This map is used for validations.
 * Created by TPa on 05.02.2023.
 */
trait ArgumentParser {

	/**
	 *
	 * @param req request
	 * @param t data case class
	 * @return Parsed request data
	 */
	def parseRequestData(req: Request, t: Class[_]): Map[Field, ParsedValue]

	/**
	 * @param p case class constructor parameter.
	 * @param validated validated object data
	 * @return required parameter object
	 */
	def mapToObject(p: Parameter, validated: Map[Field, ParsedValue]): Any = {
		p.getType match {
			case STRING => validated.find(_._1.getName == p.getName).map(_._2.stringValue).orNull
			case INTEGER | INTEGER_j => validated.find(_._1.getName == p.getName).flatMap(_._2.stringValue.toIntOption).orNull
			case DOUBLE | DOUBLE_j => validated.find(_._1.getName == p.getName).flatMap(_._2.stringValue.toDoubleOption).orNull
			case BOOLEAN | BOOLEAN_j => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toBoolean).orNull
			case FLOAT => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toFloat).orNull
			case ZONED_DATETIME => validated.find(_._1.getName == p.getName).map(fv => {
				val dateString = fv._2.stringValue
				if (dateString.isNullOrEmpty) {
					null
				} else {
					ZonedDateTime.parse(dateString)
				}
			}).orNull
			case DATE => validated.find(_._1.getName == p.getName).map(fv => {
				val dateString = fv._2.stringValue
				if (dateString.isNullOrEmpty) {
					null
				} else {
					LocalDate.parse(dateString)
				}
			}).orNull
			case SEQ =>
				val parameterizedType = p.getParameterizedType.asInstanceOf[ParameterizedType].getActualTypeArguments.head
				val constructor = Class.forName(parameterizedType.getTypeName).getDeclaredConstructors.head
				validated.filter(_._1.getName == p.getName).map(parsedValue => {

					parameterizedType match {
						case STRING =>
							val seqValue = parsedValue._2.seqValue
							seqValue.map(java.lang.String.valueOf)
						case INTEGER_j =>
							val seqValue = parsedValue._2.seqValue
							seqValue.map(java.lang.Integer.valueOf)
						case DOUBLE_j =>
							val seqValue = parsedValue._2.seqValue
							seqValue.map(java.lang.Double.valueOf)
						case _ => {
							val seqValue = parsedValue._2.seqObject
							val result = seqValue.map(v => {
								val valueMap = v.toMap[Field, ParsedValue]
								val constructorParams = constructor.getParameters.map(p => {
									mapToObject(p, valueMap)
								})
								constructor.newInstance(constructorParams: _*)
							})
							result
						}
					}
				}).head
			case x if x.isEnum =>
				val stringValue = validated.find(_._1.getName == p.getName).map(_._2.stringValue).orNull
				x.getDeclaredMethod("valueOf", classOf[String]).invoke(x, stringValue)
			case _ =>
				Logger.warn("")
				null
		}
	}

}
