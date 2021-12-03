package core

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.utils.FixedPebbleTemplateEngine
import cz.kamenitxan.jakon.webui.util.JakonFileLoader
import test.{TestBase, TestEmailTypeHandler}

class SettingsTest extends TestBase {

	test("setters") { _ =>
		val loader = new JakonFileLoader("templates/admin", true)
		loader.setSuffix(".peb")
		Settings.setAdminEngine(new FixedPebbleTemplateEngine(loader))
		Settings.setEmailTypeHandler(new TestEmailTypeHandler)
		assertThrows[IllegalArgumentException](Settings.setStaticDir(Settings.getOutputDir))
		assertThrows[IllegalArgumentException](Settings.setOutputDir(Settings.getStaticDir))
	}

	test("getters") { _ =>
		assertNotEmpty(Settings.getDatabaseDriver)
		assert(Settings.getEmailTypeHandler != null)
		assertNotEmpty(Settings.getEmailAuth)
		assertNotEmpty(Settings.getEmailHost)
		assertNotEmpty(Settings.getEmailPort)
		assertNotEmpty(Settings.getEmailUserName)
		assertNotEmpty(Settings.getEmailPassword)
		assertNotEmpty(Settings.getEmailForceBcc)
		assertNotEmpty(Settings.getLoginPath)
	}

	test("unknown deploy mode") { _ =>
		val current = Settings.getDeployMode
		Settings.setDeployMode("XXXXX")
		assert(DeployMode.PRODUCTION == Settings.getDeployMode)
		Settings.setDeployMode(current)
	}

}
