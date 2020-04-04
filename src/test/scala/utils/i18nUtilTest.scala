package utils

import java.util.Locale

import cz.kamenitxan.jakon.utils.i18nUtil
import test.TestBase

class i18nUtilTest extends TestBase {
	val en = new Locale("en", "US")
	val cs = new Locale("cs", "CZ")
	val templateDir = "templates/admin/"
	val bundle = "common"

	test("getTranslation") { _ =>
		assert("Password" == i18nUtil.getTranslation(templateDir, bundle, "PASSWORD", en))
		assert("Heslo" == i18nUtil.getTranslation(templateDir, bundle, "PASSWORD", cs))
	}

	test("getTranslation noExisting") { _ =>
		assert("TEST_TEST" == i18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", en))
		assert("TEST_TEST" == i18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", cs))
	}

	test("getTranslation default") { _ =>
		assert("def" == i18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", en, "def"))
		assert("def" == i18nUtil.getTranslation(templateDir, bundle, "TEST_TEST", cs, "def"))
	}

	test("getTranslation no budle") { _ =>
		assert("TEST_TEST" == i18nUtil.getTranslation(templateDir, "test", "TEST_TEST", en))
		assert("TEST_TEST" == i18nUtil.getTranslation(templateDir, "test", "TEST_TEST", cs))
	}

	test("getTranslation overridden") { _ =>
		assert("test_en" == i18nUtil.getTranslation(templateDir, bundle, "OVERRIDE_TEST", en))
		assert("test_cz" == i18nUtil.getTranslation(templateDir, bundle, "OVERRIDE_TEST", cs))
	}
}
