package jakontest

import jakontest.core.database.ConvertersTest
import jakontest.core.functions.FunctionHelperTest
import jakontest.core.pagelet.JsonParserTest
import jakontest.core.template.TemplateUtilsTest
import jakontest.core.template.pebble.Pebble
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
	new AdminSettingsTest,
	new JsonParserTest
) with BeforeAndAfterAll {

}
