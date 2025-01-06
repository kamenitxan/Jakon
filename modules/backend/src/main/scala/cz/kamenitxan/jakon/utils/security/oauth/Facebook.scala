package cz.kamenitxan.jakon.utils.security.oauth

import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue, Settings}
import cz.kamenitxan.jakon.utils.Utils
import io.javalin.http.Context

import java.sql.Connection


@Configuration
object Facebook extends OauthProvider {

	@ConfigurationValue(name = "OAUTH.facebook.clientId", required = false)
	var clientId: String = _
	@ConfigurationValue(name = "OAUTH.facebook.clientSecret", required = false)
	var clientSecret: String = _

	private lazy val gson = new Gson()
	val isEnabled = Utils.nonEmpty(clientId)

	def authInfo(ctx: Context, redirectTo: String) = OauthInfo("facebook", createAuthUrl(ctx))

	lazy val service = new ServiceBuilder(clientId)
		.callback(s"http://${Settings.getHostname}${
			if (Settings.getPort != 80) {
				s":${Settings.getPort}"
			}
		}/admin/login/oauth?provider=${this.getClass.getSimpleName}")
		.build(FacebookApi.instance)

	def createAuthUrl(ctx: Context): String = {
		if (!isEnabled) return ""

		val secretState = getSecretState(ctx)
		service.getAuthorizationUrl(secretState)
	}

	override def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean = {
		false
	}
}