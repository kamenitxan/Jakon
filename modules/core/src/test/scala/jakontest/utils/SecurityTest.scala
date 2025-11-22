package jakontest.utils

import cz.kamenitxan.jakon.core.database.DBHelper
import cz.kamenitxan.jakon.utils.PageContext
import cz.kamenitxan.jakon.utils.security.AesEncryptor
import cz.kamenitxan.jakon.utils.security.oauth.Google.createAuthUrl
import cz.kamenitxan.jakon.utils.security.oauth.{OauthInfo, OauthProvider}
import cz.kamenitxan.jakon.webui.entity.{Message, MessageSeverity}
import io.javalin.http.Context
import jakontest.test.TestBase
import org.scalamock.scalatest.MockFactory
import org.scalatest.DoNotDiscover

import java.sql.Connection

@DoNotDiscover
class SecurityTest extends TestBase with MockFactory {

	val fakeReq: Context = ScalalinServletContext()


	class TestOauthProvider(email: String) extends OauthProvider  {
		override val isEnabled: Boolean = true

		override def authInfo(ctx: Context, redirectTo: String): OauthInfo = OauthInfo("test", createAuthUrl(ctx, redirectTo))

		override def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean = {
			logIn(ctx, email)
		}
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
		PageContext.init(fakeReq)
		PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "")
		val provider = new TestOauthProvider("bob@test.test")
		provider.authInfo(fakeReq, "")
		provider.handleAuthResponse(fakeReq)(DBHelper.getConnection)
		PageContext.destroy()
	}

	test("test provider admin") { _ =>
		PageContext.init(fakeReq)
		PageContext.getInstance().messages += new Message(MessageSeverity.ERROR, "")
		val provider = new TestOauthProvider("admin@admin.cz")
		provider.authInfo(fakeReq, "")
		provider.handleAuthResponse(fakeReq)(DBHelper.getConnection)
		PageContext.destroy()
	}

}
