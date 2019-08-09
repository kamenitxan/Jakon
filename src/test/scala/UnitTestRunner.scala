import org.scalatest.{BeforeAndAfterAll, Suites}
import utils.UtilsTest
import validation.ValidationTest
import webui.FieldConformerTest

/**
  * Created by TPa on 27.08.16.
  */
class UnitTestRunner extends Suites(
	new UtilsTest,
	new FieldConformerTest,
	new ValidationTest
) with BeforeAndAfterAll {

}
