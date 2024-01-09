package jakontest.validation

import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.validation.Validator
import cz.kamenitxan.jakon.validation.validators.*
import cz.kamenitxan.jakon.webui.conform.FieldConformer.{DATETIME_FORMAT, DATE_FORMAT}
import org.scalatest.DoNotDiscover
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks.*
import org.scalatest.prop.TableFor2
import jakontest.utils.entity.TestObject

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

@DoNotDiscover
class ValidationUnitTest extends AnyFunSuite {

	val obj = new ValidationTestData()

	private def testTable(v: Validator, ann: Annotation, data: TableFor2[String, Boolean], obj: Map[Field, String] = null, field: Field = null): Any = {
		forAll(data) { (value, expectedResult) => {
			val res = v.isValid(value, ann, field, obj)
			assert(res.isEmpty == expectedResult)
		}
		}
	}

	private def objectToMap(o: ValidationTestData): Map[Field, String] = {
		o.getClass.getDeclaredFields.map(f => {
			f.setAccessible(true)
			val v = f.get(o).asInstanceOf[String]
			(f, v)
		}).toMap
	}

	test("notEmpty") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", true),
			(null, false),
			("", false)
		)

		val f = classOf[ValidationTestData].getDeclaredField("string")
		val ann = f.getDeclaredAnnotation(classOf[NotEmpty])
		val v = new NotEmptyValidator
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
		testTable(v, null, data)
	}

	test("size") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			("testok", true),
			("testokasdfdasfasdfadfs", false),
			(null, true)
		)


		val f = classOf[ValidationTestData].getDeclaredField("size")
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

		val f = classOf[ValidationTestData].getDeclaredField("password2")
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
		testTable(v, null, data)
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
		testTable(v, null, data)
	}

	test("null") {
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1", false)
		)

		val v = new NullValidator
		testTable(v, null, data)
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

		val f = classOf[ValidationTestData].getDeclaredField("number")
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

		val f = classOf[ValidationTestData].getDeclaredField("number")
		val ann = f.getDeclaredAnnotationsByType(classOf[Max]).head
		val v = new MaxValidator
		testTable(v, ann, data, objectToMap(new ValidationTestData()))
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
		val ann = null
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
		val ann = null
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
		val ann = null
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
		val ann = null
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


		val f = classOf[ValidationTestData].getDeclaredField("email")
		val ann = f.getDeclaredAnnotationsByType(classOf[Email]).head
		val v = new EmailValidator
		testTable(v, ann, data, objectToMap(obj))
	}

	test("past localDate") {
		val now = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", true),
			("2030-02-20", false),
			(now, false)
		)

		val v = new PastValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "localDate")
		testTable(v, ann, data, field = f)
	}

	test("past localDateTime") {
		val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", false),
			("2030-02-20", false),
			(now, false),
			("1999-02-20T01:30", true),
			("2030-02-20T01:30", false)
		)

		val v = new PastValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "localDateTime")
		testTable(v, ann, data, field = f)
	}

	test("past string") {
		val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", false),
			("2030-02-20", false),
			(now, false),
			("1999-02-20T01:30", false),
			("2030-02-20T01:30", false)
		)

		val v = new PastValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "string")
		testTable(v, ann, data, field = f)
	}

	test("future localDate") {
		val now = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", false),
			("2030-02-20", true),
			(now, false)
		)

		val v = new FutureValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "localDate")
		testTable(v, ann, data, field = f)
	}

	test("future localDateTime") {
		val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", false),
			("2030-02-20", false),
			(now, false),
			("1999-02-20T01:30", false),
			("2030-02-20T01:30", true)
		)

		val v = new FutureValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "localDateTime")
		testTable(v, ann, data, field = f)
	}

	test("future string") {
		val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_FORMAT))
		val data: TableFor2[String, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			(null, true),
			("1999-02-20", false),
			("2030-02-20", false),
			(now, false),
			("1999-02-20T01:30", false),
			("2030-02-20T01:30", false)
		)

		val v = new FutureValidator
		val ann = null
		val (_, f) = Utils.getClassByFieldName(classOf[TestObject], "string")
		testTable(v, ann, data, field = f)
	}
}
