package cz.kamenitxan.jakon.utils.security.oauth

import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue}
import cz.kamenitxan.jakon.utils.Utils
import io.javalin.http.Context

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Connection


@Configuration
object Facebook extends OauthProvider {

	@ConfigurationValue(name = "OAUTH.facebook.clientId", required = false)
	var clientId: String = scala.compiletime.uninitialized
	@ConfigurationValue(name = "OAUTH.facebook.clientSecret", required = false)
	var clientSecret: String = scala.compiletime.uninitialized

	val isEnabled: Boolean = Utils.nonEmpty(clientId)

	def authInfo(ctx: Context, redirectTo: String) = OauthInfo("facebook", createAuthUrl(ctx))

	private def enc(s: String) = URLEncoder.encode(s, StandardCharsets.UTF_8)

	def createAuthUrl(ctx: Context): String = {
		if (!isEnabled) return ""

		val secretState = getSecretState(ctx)
		s"https://www.facebook.com/v19.0/dialog/oauth?client_id=${enc(clientId)}&redirect_uri=${enc(callbackUrl)}&state=${enc(secretState)}"
	}

	override def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean = {
		false
	}
}
