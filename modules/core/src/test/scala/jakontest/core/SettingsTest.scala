package jakontest.core

import cz.kamenitxan.jakon.core.configuration.{DeployMode, Settings}
import cz.kamenitxan.jakon.core.template.utils.JakonPebbleTemplateEngine
import cz.kamenitxan.jakon.webui.util.JakonFileLoader
import jakontest.test.{TestBase, TestEmailTypeHandler}
import org.scalatest.DoNotDiscover

@DoNotDiscover
class SettingsTest extends TestBase {

	test("setters") { _ =>
		val loader = new JakonFileLoader(System.getProperty("user.dir") + "/" + "templates/admin", true)
		loader.setSuffix(".peb")
		Settings.setAdminEngine(new JakonPebbleTemplateEngine(loader))
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
		assert(Settings.getHCaptchaSiteKey == null)
		assertNotEmpty(Settings.getDeployType)
	}

	test("unknown deploy mode") { _ =>
		val current = Settings.getDeployMode
		Settings.setDeployMode("XXXXX")
		assert(DeployMode.PRODUCTION == Settings.getDeployMode)
		Settings.setDeployMode(current)
	}

	test("default emailTypeHandler") { _ =>
		val h = Settings.getEmailTypeHandler
		try {
			h.handle("")
			h.afterSend("")
		} catch {
			case _: Exception => assert(false)
		}
	}

}
