package utils

import java.sql.Connection

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.utils.security.oauth.Google.createAuthUrl
import cz.kamenitxan.jakon.utils.security.oauth.{OauthInfo, OauthProvider}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import javax.servlet.http.HttpServletRequest
import spark.Request
import test.{TestBase, TestHttpServletRequest}

class SecurityTest extends TestBase {

	class TestOauthProvider extends OauthProvider {
		override val isEnabled: Boolean = true

		override def authInfo(req: Request, redirectTo: String): OauthInfo = OauthInfo("test", createAuthUrl(req, redirectTo))

		override def handleAuthResponse(req: Request)(implicit conn: Connection): Boolean = {
			logIn(req, "bob@test.com")
		}
	}

	val fakeReq = {
		val constructor = classOf[Request].getDeclaredConstructor(Array(classOf[HttpServletRequest]): _*)
		constructor.setAccessible(true)
		val req = constructor.newInstance(new TestHttpServletRequest)

		req.session(true)
		req
	}

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

	test("test provider") { f =>
		PageContext.init(fakeReq, null)
		PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "")
		val provider = new TestOauthProvider
		provider.authInfo(fakeReq, "")
		provider.handleAuthResponse(fakeReq)(DBHelper.getConnection)
	}

}
