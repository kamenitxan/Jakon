import core.database.ConvertersTest
import core.functions.FunctionHelperTest
import core.template.TemplateUtilsTest
import core.template.pebble.Pebble
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
	new ValidationTest,
	new FunctionHelperTest,
	new Pebble,
	new TemplateUtilsTest,
	new ConvertersTest
) with BeforeAndAfterAll {

}
