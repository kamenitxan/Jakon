package jakontest.validation

import cz.kamenitxan.jakon.validation.validators.{HCaptchaValidator, UniqueValidator}
import jakontest.test.TestBase
import org.scalatest.DoNotDiscover

/**
  * Created by TPa on 09/04/2021.
  */
@DoNotDiscover
class ValidationTest extends TestBase {

	test("UniqueValidator null value") { _ =>
		val v = new UniqueValidator
		val r = v.isValid(null, null, null, null)
		assert(r.isEmpty)
	}

	test("HCaptchaValidator null value") { _ =>
		val v = new HCaptchaValidator
		val r = v.isValid(null, null, null, null)
		assert(r.nonEmpty)
	}

	test("HCaptchaValidator no secret") { _ =>
		val v = new HCaptchaValidator
		val r = v.isValid("test", null, null, null)
		assert(r.isEmpty)
	}

}
