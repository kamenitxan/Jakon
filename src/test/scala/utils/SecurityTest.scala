package utils

import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.utils.security.oauth.OauthProvider
import test.TestBase

class SecurityTest extends TestBase {

	test("encrypt&decrypt") { _ =>
		val toenc = "slaninaabůček534534345354345345345345432543254432543254325"
		val enc = AesEncryptor.encrypt(toenc)
		val dec = AesEncryptor.decrypt(enc)
		assert(toenc == dec)
	}

	test("OauthProvider") { f =>
	    assert("redirectTo" == OauthProvider.REDIRECT_TO)
	}

	test("Google") { f =>
		implicit val driver = f.driver
		f.driver.get(host + admin + "login/oauth?provider=Google$")
		checkPageLoad(".panel-title")
	}

}
