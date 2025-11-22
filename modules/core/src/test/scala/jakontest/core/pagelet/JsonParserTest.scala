package jakontest.core.pagelet

import cz.kamenitxan.jakon.core.dynamic.arguments.{CirceJsonParser, ParsedValue}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import jakontest.core.pagelet.entity.JsonDataEnum
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite

import java.lang.reflect.Field
import java.time.{LocalDate, ZonedDateTime}

@DoNotDiscover
class JsonParserTest extends AnyFunSuite {

	def parseRequestData(json: String, t: Class[_]): Map[Field, ParsedValue] = {
		val res = parser.parse(json).getOrElse(Json.Null)
		val hc: HCursor = res.hcursor

		val fields = t.getDeclaredFields
		fields.map(f => CirceJsonParser.mapToString(hc, f)).toMap[Field, ParsedValue]
	}

	def createDataObject(data: Map[Field, ParsedValue], t: Class[_]): Any = {
		val constructorParams = t.getDeclaredConstructors.head.getParameters.map(p => {
			CirceJsonParser.mapToObject(p, data)
		})

		t.getDeclaredConstructors.head.newInstance(constructorParams: _*)
	}

	def parse(json: String, t: Class[_]): Any = {
		val data = parseRequestData(json, t)
		createDataObject(data, t)
	}

	test("string") {
		val data = parse("""{"name": "test"}""", classOf[StringData]).asInstanceOf[StringData]
		assert(data.name == "test")
	}

	test("number to string") {
		val data = parse("""{"name": 42}""", classOf[StringData]).asInstanceOf[StringData]
		assert(data.name == "42")
	}

	test("boolean to string") {
		val data = parse("""{"name": true}""", classOf[StringData]).asInstanceOf[StringData]
		assert(data.name == "true")
		val data2 = parse("""{"name": false}""", classOf[StringData]).asInstanceOf[StringData]
		assert(data2.name == "false")
	}

	test("int") {
		val data = parse("""{"v": 42}""", classOf[IntData]).asInstanceOf[IntData]
		assert(data.v == 42)
	}
	test("int J") {
		val data = parse("""{"v": 42}""", classOf[IntJData]).asInstanceOf[IntJData]
		assert(data.v == 42)
	}


	test("double") {
		val data = parse("""{"v": 42.4}""", classOf[DoubleData]).asInstanceOf[DoubleData]
		assert(data.v == 42.4)
	}

	test("double J") {
		val data = parse("""{"v": 42.4}""", classOf[DoubleJData]).asInstanceOf[DoubleJData]
		assert(data.v == 42.4)
	}

	test("float") {
		val data = parse("""{"v": 42.4}""", classOf[FloatData]).asInstanceOf[FloatData]
		assert(data.v == 42.4f)
	}

	test("boolean") {
		val data = parse("""{"v": true}""", classOf[BooleanData]).asInstanceOf[BooleanData]
		assert(data.v)
		val data2 = parse("""{"v": false}""", classOf[BooleanData]).asInstanceOf[BooleanData]
		assert(!data2.v)
	}

	test("boolean J") {
		val data = parse("""{"v": true}""", classOf[BooleanJData]).asInstanceOf[BooleanJData]
		assert(data.v)
		val data2 = parse("""{"v": false}""", classOf[BooleanJData]).asInstanceOf[BooleanJData]
		assert(!data2.v)
	}

	test("seq string") {
		val data = parse("""{"v": ["a","b"]}""", classOf[StringSeqData]).asInstanceOf[StringSeqData]
		assert(data.v.length == 2)
		assert(data.v.contains("a"))
		assert(data.v.contains("b"))
	}

	test("seq int J") {
		val data = parse("""{"v": [1,2]}""", classOf[IntJSeqData]).asInstanceOf[IntJSeqData]
		assert(data.v.length == 2)
		assert(data.v.contains(1))
		assert(data.v.contains(2))
	}

	test("seq double J") {
		val data = parse("""{"v": [1.0,2.4]}""", classOf[DoubleJSeqData]).asInstanceOf[DoubleJSeqData]
		assert(data.v.length == 2)
		assert(data.v.contains(1))
		assert(data.v.contains(2.4))
	}

	test("seq StringData") {
		val data = parse("""{"v": [{"name": "a"},{"name": "b"}]}""", classOf[StringDataSeqData]).asInstanceOf[StringDataSeqData]
		assert(data.v.length == 2)
		assert(data.v.head.name == "a")
		assert(data.v.last.name == "b")
	}

	test("enum") {
		val data = parse("""{"v": "B"}""", classOf[EnumData]).asInstanceOf[EnumData]
		assert(data.v == JsonDataEnum.B)
	}

	test("ZonedDateTime") {
		val now = ZonedDateTime.now()
		val data = parse(s"""{"v": "${now.toString}"}""", classOf[ZonedDateTimeData]).asInstanceOf[ZonedDateTimeData]
		assert(data.v == now)
		val data2 = parse(s"""{"v": ""}""", classOf[ZonedDateTimeData]).asInstanceOf[ZonedDateTimeData]
		assert(data2.v == null)
	}

	test("LocalDate") {
		val now = LocalDate.now()
		val data = parse(s"""{"v": "${now.toString}"}""", classOf[LocalDateData]).asInstanceOf[LocalDateData]
		assert(data.v == now)
		val data2 = parse(s"""{"v": ""}""", classOf[LocalDateData]).asInstanceOf[LocalDateData]
		assert(data2.v == null)
	}

}

case class StringData(name: String)
case class IntData(v: Int)
case class IntJData(v: java.lang.Integer)
case class DoubleData(v: Double)
case class DoubleJData(v: java.lang.Double)
case class FloatData(v: Float)
case class BooleanData(v: Boolean)
case class BooleanJData(v: java.lang.Boolean)
case class StringSeqData(v: Seq[String])
case class IntJSeqData(v: Seq[java.lang.Integer])
case class DoubleJSeqData(v: Seq[java.lang.Double])
case class StringDataSeqData(v: Seq[StringData])
case class EnumData(v: JsonDataEnum)
case class ZonedDateTimeData(v: ZonedDateTime)
case class LocalDateData(v: LocalDate)