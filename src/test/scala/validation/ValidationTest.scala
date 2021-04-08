package validation

import cz.kamenitxan.jakon.validation.validators.UniqueValidator
import test.TestBase

/**
  * Created by TPa on 09/04/2021.
  */
class ValidationTest extends TestBase {

	test("UniqueValidator null value") { _ =>
		val v = new UniqueValidator
		val r = v.isValid(null, null, null, null)
		assert(r.isEmpty)
	}

}
