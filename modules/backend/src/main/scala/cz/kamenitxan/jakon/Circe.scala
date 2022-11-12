package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.configuration.DeployMode
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.utils.Utils.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.conform.GenericType
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*

import java.lang.reflect.{Field, Parameter, ParameterizedType}
import java.time.ZonedDateTime

/**
	* Created by TPa on 30.08.2022.
	*/
object Circe {
	def main(args: Array[String]): Unit = {
		val rawJson: String =
			"""
				| {
				| "id": 42,
				| "name": "bar",
				| "double": 2.0,
				| "deployMode": "DEVEL",
				| "inner": [{"name": "test", "otherName": "otherName"}, {"name": "test2", "otherName": "otherName2"}],
				| "seqq": [6,7.2],
				| "parent": {
				| 	"id": 42,
				|   "name": "bar"
				|  }
				| }
				|""".stripMargin

		println(rawJson)

		val res = parser.parse(rawJson).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor
		//println(res)

		val fields = classOf[Test].getDeclaredFields
		val maped = fields.map(f => mapToString(hc, f)).toMap[Field, ParsedValue]

		//EntityValidator.validate("", maped)

		val validated: Map[Field, ParsedValue] = maped
		val contructorParams = classOf[Test].getDeclaredConstructors.head.getParameters.map(p => {
			mapToValue(p, validated)
		})

		val resClass = classOf[Test].getDeclaredConstructors.head.newInstance(contructorParams:_*).asInstanceOf[Test]

		resClass.inner.foreach(i => println(i.name))

		println("gagga")

	}

	def parseJsonData(json: String, t: Class[_]): Any = {
		val res = parser.parse(json).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor

		val fields = t.getDeclaredFields
		val maped: Map[Field, ParsedValue] = fields.map(f => mapToString(hc, f)).toMap[Field, ParsedValue]

		//EntityValidator.validate("", maped)

		val validated: Map[Field, ParsedValue] = maped
		val contructorParams = t.getDeclaredConstructors.head.getParameters.map(p => {
			mapToValue(p, validated)
		})

		t.getDeclaredConstructors.head.newInstance(contructorParams: _*)
	}

	def mapToString(hc: HCursor, f: Field): (Field, ParsedValue) = {
		val name = f.getName
		val value = f.getType match
			case STRING | ZONED_DATETIME => ParsedValue(hc.downField(name).focus.flatMap(_.asString).getOrElse(""), null,null, null)
			case INTEGER | DOUBLE | FLOAT | DOUBLE_j | INTEGER_j => ParsedValue(hc.downField(name).focus.map(v => {
				v.toString.replace("\"", "").replace("\'", "")
			}).getOrElse(""), null, null, null)
			case BOOLEAN => ParsedValue(hc.downField(name).focus.flatMap(_.asBoolean).getOrElse(false).toString, null, null, null)
			case SEQ =>
				val objArr = hc.downField(name).focus.flatMap(_.asArray).getOrElse(Vector.empty)
				val seqTypeConstructor = f.getDeclaringClass.getDeclaredConstructors.head
				val seqTypeParams = seqTypeConstructor.getParameters.map(_.getName)
				val gft = seqTypeConstructor.getGenericParameterTypes.drop(seqTypeParams.indexOf(name)).head.asInstanceOf[ParameterizedType].getActualTypeArguments
				val seqType = gft.headOption.orNull
				val seqTypeFields = Class.forName(seqType.getTypeName).getDeclaredFields

				seqType match
					case INTEGER | DOUBLE | FLOAT | DOUBLE_j | INTEGER_j => ParsedValue(null, null, objArr.map(_.toString), null)
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

	case class ParsedValue(
													stringValue: String,
													seqObject: Seq[Seq[(Field, ParsedValue)]],
													seqValue: Seq[String],
													objectValue: Map[Field, ParsedValue]
												)

	def mapToValue(p: Parameter, validated: Map[Field, ParsedValue]): Any = {
		val value = p.getType match
			case STRING => validated.find(_._1.getName == p.getName).map(_._2.stringValue).orNull
			case INTEGER => validated.find(_._1.getName == p.getName).flatMap(_._2.stringValue.toIntOption).orNull
			case DOUBLE => validated.find(_._1.getName == p.getName).flatMap(_._2.stringValue.toDoubleOption).orNull
			case BOOLEAN => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toBoolean).orNull
			case FLOAT => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toFloat).orNull
			case ZONED_DATETIME => validated.find(_._1.getName == p.getName).map(fv => {
				val dateString = fv._2.stringValue
				if (dateString.isNullOrEmpty) {
					null
				} else {
					ZonedDateTime.parse(dateString)
				}
			}).orNull
			case SEQ =>
				val parameterizedType = p.getParameterizedType.asInstanceOf[ParameterizedType].getActualTypeArguments.head
				val constructor = Class.forName(parameterizedType.getTypeName).getDeclaredConstructors.head
				validated.filter(_._1.getName == p.getName).map(parsedValue => {

					parameterizedType match
						case INTEGER_j =>
							val seqValue = parsedValue._2.seqValue
							seqValue.map(java.lang.Integer.valueOf)
						case DOUBLE_j =>
							val seqValue = parsedValue._2.seqValue
							seqValue.map(java.lang.Double.valueOf).toSeq
						case _ => {
							val seqValue = parsedValue._2.seqObject
							val result = seqValue.map(v => {
								val valueMap = v.toMap[Field, ParsedValue]
								val contructorParams = constructor.getParameters.map(p => {
									mapToValue(p, valueMap)
								})
								constructor.newInstance(contructorParams: _*)
							})
							result
						}
				}).head
			case x if x.isEnum =>
				val stringValue = validated.find(_._1.getName == p.getName).map(_._2.stringValue).orNull
				x.getDeclaredMethod("valueOf", classOf[String]).invoke(x, stringValue)
			case _ => null
		value
	}


}

case class Inner(
									name: String,
									otherName: String
								)

case class Test(id: Int,
								name: String,
								deployMode: DeployMode,
								double: Double,
								inner: Seq[Inner],
								seqq: Seq[java.lang.Double],
								parent: Test
							 )