package cz.kamenitxan.jakon.core.dynamic.arguments

import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.TypeReferences.*
import io.circe.*
import io.circe.parser.*
import io.javalin.http.Context

import java.lang.reflect.{Field, ParameterizedType}

/**
 * Created by TPa on 30.08.2022.
 */
object CirceJsonParser extends ArgumentParser {

	override def parseRequestData(ctx: Context, t: Class[_]): Map[Field, ParsedValue] = {
		val res = parser.parse(ctx.body()).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor

		val fields = t.getDeclaredFields
		fields.map(f => CirceJsonParser.mapToString(hc, f)).toMap[Field, ParsedValue]
	}

	def mapToString(hc: HCursor, f: Field): (Field, ParsedValue) = {
		val name = f.getName
		val value = f.getType match {
			case STRING | ZONED_DATETIME | DATE=> ParsedValue(hc.downField(name).focus.flatMap(jsonObject => {
				if (jsonObject.isBoolean) {
					jsonObject.asBoolean.map(_.toString)
				} else if (jsonObject.isNumber) {
					jsonObject.asNumber.map(_.toString)
				} else {
					jsonObject.asString
				}
			}).getOrElse(""), null, null, null)
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
				seqType match {
					case INTEGER | DOUBLE | FLOAT | DOUBLE_j | INTEGER_j => ParsedValue(null, null, objArr.map(_.toString), null)
					case STRING => ParsedValue(null, null, objArr.map(_.asString.getOrElse("")), null)
					case _ =>
						val rr = objArr.map(o => seqTypeFields.map(f2 => mapToString(o.hcursor, f2)).toSeq)
						ParsedValue(null, rr, null, null)
				}
			case x if x.isEnum =>
				val jsonField = hc.downField(name).focus
				jsonField
					.map(f => ParsedValue(f.asString.getOrElse(""), null, null, null))
					.getOrElse(ParsedValue(null, null, null, null))

			case _ =>
				Logger.warn(s"Unknow type: ${f.getType.getName}")
				ParsedValue(null, null, null, null)
		}

		(f -> value)
	}


}
