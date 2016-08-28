import cz.kamenitxan.jakon.utils.validation.validators.{NotEmptyValidator, NotNullValidator}
import org.scalatest.FunSuite

/**
  * Created by TPa on 28.08.16.
  */
class Validators extends FunSuite {

	test("NotNullValidatorTest") {
		val validator = new NotNullValidator
		//assertResult(false)(validator.isValid(null))
		assertResult(true)(validator.isValid(validator))
	}

	test("NotEmptyValidatorTest") {
		val validator = new NotEmptyValidator
		validator.isValid("test")
	}
}
