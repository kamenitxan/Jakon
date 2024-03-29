package jakontest.utils

import cz.kamenitxan.jakon.utils.Utils
import org.scalatest.DoNotDiscover
import jakontest.test.TestBase

/**
 * Created by TPa on 20.06.2021.
 */
@DoNotDiscover
class UtilsTest extends TestBase {

	test("loadEmailTemplate") { _ =>
		val tmpl = Utils.loadEmailTemplate("TEST", "FROM", "SUBJ", "jakon_config_test_dev.properties")
		assert(tmpl.id > 0)
		assert(tmpl.name == "TEST")
		assert(tmpl.from == "FROM")
		assert(tmpl.subject == "SUBJ")
		assert(tmpl.template.contains("databaseConnPath"))
	}

}
