package jakontest.utils

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.utils.security.oauth.Google.createAuthUrl
import cz.kamenitxan.jakon.utils.security.oauth.{OauthInfo, OauthProvider}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import jakarta.servlet.http.HttpServletRequest
import jakontest.test.{TestBase, TestHttpServletRequest}
import org.scalatest.DoNotDiscover
import spark.Request

import java.sql.Connection

@DoNotDiscover
class SecurityTest extends TestBase {

	class TestOauthProvider(email: String) extends OauthProvider {
		override val isEnabled: Boolean = true

		override def authInfo(req: Request, redirectTo: String): OauthInfo = OauthInfo("test", createAuthUrl(req, redirectTo))

		override def handleAuthResponse(req: Request)(implicit conn: Connection): Boolean = {
			logIn(req, email)
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
		val toenc = "slaninaabucek534534345354345345345345432543254432543254325"
		val enc = AesEncryptor.encrypt(toenc)
		val dec = AesEncryptor.decrypt(enc)
		assert(toenc == dec)
	}

	test("OauthProvider") { _ =>
	    assert("redirectTo" == OauthProvider.REDIRECT_TO)
	}

	test("Google") { f =>
		implicit val driver = f.driver
		f.driver.get(adminHost + "login/oauth?provider=Google$")
		checkPageLoad(".card-title")
	}

	test("test provider bob") { _ =>
		PageContext.init(fakeReq, null)
		PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "")
		val provider = new TestOauthProvider("bob@test.test")
		provider.authInfo(fakeReq, "")
		provider.handleAuthResponse(fakeReq)(DBHelper.getConnection)
		PageContext.destroy()
	}

	test("test provider admin") { _ =>
		PageContext.init(fakeReq, null)
		PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "")
		val provider = new TestOauthProvider("admin@admin.cz")
		provider.authInfo(fakeReq, "")
		provider.handleAuthResponse(fakeReq)(DBHelper.getConnection)
		PageContext.destroy()
	}

}
