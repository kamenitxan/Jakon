import org.scalatest.{BeforeAndAfterAll, Suites}
import validation.ValidationTest

/**
  * Created by TPa on 27.08.16.
  */
class UnitTestRunner extends Suites(
	new ValidationTest
) with BeforeAndAfterAll {

}
