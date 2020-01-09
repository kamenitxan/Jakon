import functions.FunctionHelperTest
import org.scalatest.{BeforeAndAfterAll, Suites}
import template.pebble.Pebble
import utils.UtilsTest
import validation.ValidationTest
import webui.FieldConformerTest

/**
  * Created by TPa on 27.08.16.
  */
class UnitTestRunner extends Suites(
	new UtilsTest,
	new FieldConformerTest,
	new ValidationTest,
	new FunctionHelperTest,
	new Pebble
) with BeforeAndAfterAll {

}
