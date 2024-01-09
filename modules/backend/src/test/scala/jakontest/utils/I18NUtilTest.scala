package jakontest.utils

import java.util.Locale
import cz.kamenitxan.jakon.utils.I18nUtil
import org.scalatest.DoNotDiscover
import jakontest.test.TestBase

@DoNotDiscover
class I18NUtilTest extends TestBase {
	val en = new Locale("en", "US")
	val cs = new Locale("cs", "CZ")
	val templateDir = "templates/admin/"
	val bundle = "common"

	test("getTranslation") { _ =>
		assert("Password" == I18nUtil.getTranslation(templateDir, bundle, "PASSWORD", en))
		assert("Heslo" == I18nUtil.getTranslation(templateDir, bundle, "PASSWORD", cs))
	}

	test("getTranslation noExisting") { _ =>
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", en))
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", cs))
	}

	test("getTranslation noExisting bundle") { _ =>
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, "invalid", "TEST_TEST", en))
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, "invalid", "TEST_TEST", cs))
	}

	test("getTranslation default") { _ =>
		assert("def" == I18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", en, "def"))
		assert("def" == I18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", cs, "def"))
	}

	test("getTranslation no budle") { _ =>
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, "test", "TEST_TEST", en))
		assert("TEST_TEST" == I18nUtil.getTranslation(templateDir, "test", "TEST_TEST", cs))
	}

	test("getTranslation overridden") { _ =>
		assert("test_en" == I18nUtil.getTranslation(templateDir, bundle, "OVERRIDE_TEST", en))
		assert("test_cz" == I18nUtil.getTranslation(templateDir, bundle, "OVERRIDE_TEST", cs))
	}

	test("getTranslation only en") { _ =>
		assert("test_en" == I18nUtil.getTranslation(templateDir, bundle, "ONLY_EN_TEST", cs))
	}
}
