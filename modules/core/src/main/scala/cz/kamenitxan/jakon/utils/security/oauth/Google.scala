package cz.kamenitxan.jakon.utils.security.oauth

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}
import cz.kamenitxan.jakon.logging.Logger
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.Utils.StringImprovements
import io.javalin.http.Context
import org.pac4j.core.context.CallContext
import org.pac4j.core.exception.http.WithLocationAction
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver
import org.pac4j.javalin.JavalinWebContext
import org.pac4j.jee.context.session.JEESessionStore
import org.pac4j.oidc.client.GoogleOidcClient
import org.pac4j.oidc.config.OidcConfiguration

import java.sql.Connection
import scala.jdk.OptionConverters.*
import scala.util.control.NonFatal


/**
 * Google login through pac4j's OpenID Connect client. pac4j keeps the state, nonce and PKCE verifier in the session
 * and validates the ID token, the e-mail comes from its claims.
 */
@Configuration
object Google extends OauthProvider {

	@ConfigurationValue(name = "OAUTH.google.clientId", required = false)
	var clientId: String = scala.compiletime.uninitialized
	@ConfigurationValue(name = "OAUTH.google.clientSecret", required = false)
	var clientSecret: String = scala.compiletime.uninitialized

	val isEnabled: Boolean = Utils.nonEmpty(clientId) && Utils.nonEmpty(clientSecret)

	private val sessionStore = new JEESessionStore()

	def authInfo(ctx: Context, redirectTo: String) = OauthInfo("google", createAuthUrl(ctx, redirectTo))

	lazy val client: GoogleOidcClient = {
		val config = new OidcConfiguration()
		config.setClientId(clientId)
		config.setSecret(clientSecret)
		config.setScope("openid email")
		val c = new GoogleOidcClient(config)
		c.setCallbackUrl(callbackUrl)
		// keep the registered redirect URI as it is, pac4j would add ?client_name=... to it
		c.setCallbackUrlResolver(new NoParameterCallbackUrlResolver())
		c
	}

	private def callContext(ctx: Context) = new CallContext(new JavalinWebContext(ctx), sessionStore)

	def createAuthUrl(ctx: Context, redirectTo: String): String = {
		if (!isEnabled) return ""

		// the provider does not return our own parameters, the target page waits in the session
		if (redirectTo != null) ctx.sessionAttribute(OauthProvider.REDIRECT_TO, redirectTo)
		client.getRedirectionAction(callContext(ctx)).toScala match {
			case Some(action: WithLocationAction) => action.getLocation
			case _ => ""
		}
	}

	override def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean = {
		if (!isEnabled || ctx.queryParam("code").isNullOrEmpty) {
			return false
		}

		try {
			val cc = callContext(ctx)
			val email = for {
				credentials <- client.getCredentials(cc).toScala
				validated <- client.validateCredentials(cc, credentials).toScala
				profile <- client.getUserProfile(cc, validated).toScala
				email <- Option(profile.getAttribute("email")).map(_.toString)
				if email.nonEmpty
			} yield email
			email.exists(logIn(ctx, _))
		} catch {
			case NonFatal(e) =>
				Logger.error("Google login failed", e)
				false
		}
	}
}
