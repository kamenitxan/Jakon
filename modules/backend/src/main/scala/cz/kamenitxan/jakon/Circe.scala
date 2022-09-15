package cz.kamenitxan.jakon

import cz.kamenitxan.jakon.core.configuration.DeployMode
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import cz.kamenitxan.jakon.utils.TypeReferences.*
import cz.kamenitxan.jakon.validation.EntityValidator
import cz.kamenitxan.jakon.webui.conform.GenericType

import java.lang.reflect.{Field, Parameter, ParameterizedType}

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
			case STRING => ParsedValue(hc.downField(name).focus.get.asString.getOrElse(""), null, null)
			case INTEGER | DOUBLE => ParsedValue(hc.downField(name).focus.map(v => {
				v.toString.replace("\"", "").replace("\'", "")
			}).getOrElse(""), null, null)
			case SEQ =>
				println("seq")
				val objArr = hc.downField("inner").focus.flatMap(_.asArray).getOrElse(Vector.empty)
				val seqTypeConstructor = f.getDeclaringClass.getDeclaredConstructors.head
				val seqTypeParams = seqTypeConstructor.getParameters.map(_.getName)
				val gft = seqTypeConstructor.getGenericParameterTypes.drop(seqTypeParams.indexOf(name)).head.asInstanceOf[ParameterizedType].getActualTypeArguments
				val seqType = gft.headOption.orNull
				val seqTypeFields = Class.forName(seqType.getTypeName).getDeclaredFields

				val rr = objArr.map(o => seqTypeFields.map(f2 => mapToString(o.hcursor, f2)).toSeq).toSeq

				ParsedValue(null, rr, null)
			case x if x.isEnum =>
				ParsedValue(hc.downField(name).focus.get.asString.getOrElse(""), null, null)
			case _ =>
				println(f.getType.getName)
				ParsedValue(null, null, null)
		(f -> value)
	}

	case class ParsedValue(
													stringValue: String,
													seqValue: Seq[Seq[(Field, ParsedValue)]],
													objectValue: Map[Field, ParsedValue]
												)

	def mapToValue(p: Parameter, validated: Map[Field, ParsedValue]): Any = {
		val value = p.getType match
			case STRING => validated.find(_._1.getName == p.getName).map(_._2.stringValue).orNull
			case INTEGER => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toInt).orNull
			case DOUBLE => validated.find(_._1.getName == p.getName).map(_._2.stringValue.toDouble).orNull
			case SEQ =>
				val parameterizedType = p.getParameterizedType.asInstanceOf[ParameterizedType].getActualTypeArguments.head
				val constructor = Class.forName(parameterizedType.getTypeName).getDeclaredConstructors.head
				validated.filter(_._1.getName == p.getName).map(parsedValue => {
					val seqValue = parsedValue._2.seqValue

					val result = seqValue.map(v => {
						val valueMap = v.toMap[Field, ParsedValue]
						val contructorParams = constructor.getParameters.map(p => {
							mapToValue(p, valueMap)
						})
						constructor.newInstance(contructorParams:_*)
					})
					result
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
								parent: Test
							 )