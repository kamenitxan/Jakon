package utils

import java.util.Locale

import cz.kamenitxan.jakon.utils.i18nUtil
import test.TestBase

class i18nUtilTest extends TestBase {
	val en = new Locale("en", "US")
	val cs = new Locale("cs", "CZ")
	val bundle = "templates/admin/common"

	test("getTranslation") { _ =>
		assert("Password" == i18nUtil.getTranslation(bundle, "PASSWORD", en))
		assert("Heslo" == i18nUtil.getTranslation(bundle, "PASSWORD", cs))
	}

	test("getTranslation noExisting") { _ =>
		assert("TEST_TEST" == i18nUtil.getTranslation(bundle, "TEST_TEST", en))
		assert("TEST_TEST" == i18nUtil.getTranslation(bundle, "TEST_TEST", cs))
	}

	test("getTranslation default") { _ =>
		assert("def" == i18nUtil.getTranslation(bundle, "TEST_TEST", en, "def"))
		assert("def" == i18nUtil.getTranslation(bundle, "TEST_TEST", cs, "def"))
	}

	test("getTranslation no budle") { _ =>
		assert("TEST_TEST" == i18nUtil.getTranslation("test", "TEST_TEST", en))
		assert("TEST_TEST" == i18nUtil.getTranslation("test", "TEST_TEST", cs))
	}
}
