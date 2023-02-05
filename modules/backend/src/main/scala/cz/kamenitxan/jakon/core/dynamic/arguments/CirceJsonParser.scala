package cz.kamenitxan.jakon.core.dynamic.arguments

import cz.kamenitxan.jakon.core.configuration.DeployMode
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.utils.Utils.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.conform.GenericType
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import spark.Request

import java.lang.reflect.{Field, Parameter, ParameterizedType}
import java.time.ZonedDateTime

/**
	* Created by TPa on 30.08.2022.
	*/
object CirceJsonParser extends ArgumentParser {

	override def parseRequestData(req: Request, t: Class[_]): Map[Field, ParsedValue] = {
		val res = parser.parse(req.body()).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor

		val fields = t.getDeclaredFields
		fields.map(f => CirceJsonParser.mapToString(hc, f)).toMap[Field, ParsedValue]
	}
	
	def mapToString(hc: HCursor, f: Field): (Field, ParsedValue) = {
		val name = f.getName
		val value = f.getType match
			case STRING | ZONED_DATETIME => ParsedValue(hc.downField(name).focus.flatMap(_.asString).getOrElse(""), null,null, null)
			case INTEGER | DOUBLE | FLOAT | DOUBLE_j | INTEGER_j => ParsedValue(hc.downField(name).focus.map(v => {
				v.toString.replace("\"", "").replace("\'", "")
			}).getOrElse(""), null, null, null)
			case BOOLEAN | BOOLEAN_j => ParsedValue(hc.downField(name).focus.flatMap(_.asBoolean).getOrElse(false).toString, null, null, null)
			case SEQ =>
				val objArr = hc.downField(name).focus.flatMap(_.asArray).getOrElse(Vector.empty)
				val seqTypeConstructor = f.getDeclaringClass.getDeclaredConstructors.head
				val seqTypeParams = seqTypeConstructor.getParameters.map(_.getName)
				val gft = seqTypeConstructor.getGenericParameterTypes.drop(seqTypeParams.indexOf(name)).head.asInstanceOf[ParameterizedType].getActualTypeArguments
				val seqType = gft.headOption.orNull
				val seqTypeFields = Class.forName(seqType.getTypeName).getDeclaredFields

				seqType match
					case INTEGER | DOUBLE | FLOAT | DOUBLE_j | INTEGER_j => ParsedValue(null, null, objArr.map(_.toString), null)
					case STRING => ParsedValue(null, null, objArr.map(_.asString.getOrElse("")), null)
					case _ =>
						val rr = objArr.map(o => seqTypeFields.map(f2 => mapToString(o.hcursor, f2)).toSeq)
						ParsedValue(null, rr, null, null)

			case x if x.isEnum =>
				val jsonField = hc.downField(name).focus
				jsonField
					.map(f => ParsedValue(f.asString.getOrElse(""), null, null, null))
					.getOrElse(ParsedValue(null, null, null, null))

			case _ =>
				println(f.getType.getName)
				ParsedValue(null, null, null, null)
		(f -> value)
	}
	

}
