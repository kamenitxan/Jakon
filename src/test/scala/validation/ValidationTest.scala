package validation

import java.lang.annotation.Annotation
import java.lang.reflect.Field

import cz.kamenitxan.jakon.validation.Validator
import cz.kamenitxan.jakon.validation.validators._
import org.scalatest.FunSuite
import org.scalatest.prop.TableDrivenPropertyChecks.{forAll, _}
import org.scalatest.prop.TableFor2
import sun.reflect.annotation.AnnotationParser

class ValidationTest extends FunSuite {

	private def testTable(v: Validator, ann: Annotation, data: TableFor2[String, Boolean], obj: Map[Field, String] = null) = {
		forAll(data) { (value, expectedResult) => {
			val res = v.isValid(value, ann, obj)
			assert(res.isEmpty == expectedResult)
		}
		}
	}

	private def objectToMap(o: AnyRef): Map[Field, String] = {
		o.getClass.getDeclaredFields.map(f => {
			(f, f.get(o).toString)
		}).toMap
	}

	test("notEmpty") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", true),
			(null, false),
			("", false)
		)

		val v = new NotEmptyValidator
		val ann = AnnotationParser.annotationForMap(classOf[NotEmpty], null)
		testTable(v, ann, data)
	}

	test("dummy") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", true),
			(null, true),
			("", true)
		)

		val v = new DummyValidator
		val ann = AnnotationParser.annotationForMap(classOf[NotEmpty], null)
		testTable(v, ann, data)
	}

	test("size") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			("testok", true),
			("testokasdfdasfasdfadfs", false),
			(null, true)
		)

		class SizeTest {
			@Size(min = 5, max = 10)
			var size: String = _
		}
		val f = classOf[SizeTest].getDeclaredField("size")
		val ann = f.getDeclaredAnnotationsByType(classOf[Size]).head
		val v = new SizeValidator
		testTable(v, ann, data)
	}

	test("equalsWithOther") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			("testok", true),
			(null, true)
		)

		class TestData {
			var password: String = "testok"
			@EqualsWithOther(value = "password")
			var password2: String = _
		}
		val obj = new TestData()
		val f = classOf[TestData].getDeclaredField("password2")
		val ann = f.getDeclaredAnnotationsByType(classOf[EqualsWithOther]).head
		val v = new EqualsWithOtherValidator
		testTable(v, ann, data, objectToMap(obj))
	}

	test("true") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1", true),
			("true", true),
			("0", false),
			("false", false)
		)

		val v = new AssertTrueValidator
		val ann = AnnotationParser.annotationForMap(classOf[AssertTrue], null)
		testTable(v, ann, data)
	}

	test("false") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1", false),
			("true", false),
			("0", true),
			("false", true)
		)

		val v = new AssertFalseValidator
		val ann = AnnotationParser.annotationForMap(classOf[AssertFalse], null)
		testTable(v, ann, data)
	}

	test("null") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1", false)
		)

		val v = new NullValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.Null], null)
		testTable(v, ann, data)
	}

	test("min") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("text", false),
			("0", false),
			("5", true),
			("10", true),
			("0.0", false),
			("5.0", true),
			("10.0", true),
			(null, true)
		)

		class TestData {
			@Min(value = 5)
			var number: String = _
		}
		val obj = new TestData()
		val f = classOf[TestData].getDeclaredField("number")
		val ann = f.getDeclaredAnnotationsByType(classOf[Min]).head
		val v = new MinValidator
		testTable(v, ann, data, objectToMap(obj))
	}

	test("max") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("text", false),
			("0", true),
			("5", true),
			("10", false),
			("0.0", true),
			("5.0", true),
			("10.0", false),
			(null, true)
		)

		class TestData {
			@Max(value = 5)
			var number: String = _
		}
		val obj = new TestData()
		val f = classOf[TestData].getDeclaredField("number")
		val ann = f.getDeclaredAnnotationsByType(classOf[Max]).head
		val v = new MaxValidator
		testTable(v, ann, data, objectToMap(obj))
	}

	test("positive") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("-1", false),
			("0", false),
			("1", true)
		)

		val v = new PositiveValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.Positive], null)
		testTable(v, ann, data)
	}

	test("positiveOrZero") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("-1", false),
			("0", true),
			("1", true)
		)

		val v = new PositiveOrZeroValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.PositiveOrZero], null)
		testTable(v, ann, data)
	}

	test("negative") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("-1", true),
			("0", false),
			("1", false)
		)

		val v = new NegativeValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.Negative], null)
		testTable(v, ann, data)
	}

	test("negativeOrZero") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("-1", true),
			("0", true),
			("1", false)
		)

		val v = new NegativeOrZeroValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.NegativeOrZero], null)
		testTable(v, ann, data)
	}

	test("email") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("bob@gmail.com", true),
			("bob@gnail.com", false),
			("bob@nan.nan", false),
			("bob@10mail.com", false)
		)

		val v = new EmailValidator
		val ann = AnnotationParser.annotationForMap(classOf[cz.kamenitxan.jakon.validation.validators.Email], null)
		testTable(v, ann, data)
	}
}
