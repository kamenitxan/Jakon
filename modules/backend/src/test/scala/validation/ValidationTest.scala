package validation

import cz.kamenitxan.jakon.validation.validators.UniqueValidator
import org.scalatest.DoNotDiscover
import test.TestBase

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

}
