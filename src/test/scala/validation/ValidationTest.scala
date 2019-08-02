package validation

import java.lang.annotation.Annotation

import cz.kamenitxan.jakon.validation.Validator
import cz.kamenitxan.jakon.validation.validators.{DummyValidator, NotEmpty, NotEmptyValidator, Size, SizeValidator}
import org.scalatest.FunSuite
import org.scalatest.prop.TableDrivenPropertyChecks.{forAll, _}
import org.scalatest.prop.TableFor2
import sun.reflect.annotation.AnnotationParser

class ValidationTest extends FunSuite{

	private def testTable(v: Validator, ann: Annotation, data: TableFor2[Any, Boolean]) = {
		forAll(data) { (value, expectedResult) => {
			val res = v.isValid(value, ann, null)
			assert(res.isEmpty == expectedResult)
		}}
	}

	test("notEmpty") {
		val data: TableFor2[Any, Boolean] = Table(
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
		val data: TableFor2[Any, Boolean] = Table(
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
		val data: TableFor2[Any, Boolean] = Table(
			("value", "expectedResult"),
			("test", false),
			("testok", true),
			("testokasdfdasfasdfadfs", false),
			(null, true)
		)

		class SizeTest {
			@Size(min = 5, max = 10)
			var size: Int = _
		}
		val f = classOf[SizeTest].getDeclaredField("size")
		val ann = f.getDeclaredAnnotationsByType(classOf[Size]).head
		val v = new SizeValidator
		testTable(v, ann, data)
	}
}
