import core.database.ConvertersTest
import core.functions.FunctionHelperTest
import core.template.TemplateUtilsTest
import core.template.pebble.Pebble
import org.scalatest.{BeforeAndAfterAll, Suites}
import utils.UtilsUnitTest
import validation.ValidationUnitTest
import webui.{AdminSettingsTest, FieldConformerTest}

/**
  * Created by TPa on 27.08.16.
  */
class UnitTestRunner extends Suites(
	new UtilsUnitTest,
	new FieldConformerTest,
	new ValidationUnitTest,
	new FunctionHelperTest,
	new Pebble,
	new TemplateUtilsTest,
	new ConvertersTest,
	new AdminSettingsTest
) with BeforeAndAfterAll {

}
