package cz.kamenitxan.jakon.utils.security.oauth

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.{OAuth2AccessToken, OAuthRequest, Verb}
import com.google.gson.Gson
import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue, Settings}
import cz.kamenitxan.jakon.utils.Utils
import cz.kamenitxan.jakon.utils.Utils.StringImprovements
import io.javalin.http.Context

import java.sql.Connection
import java.util
import scala.jdk.CollectionConverters.*


@Configuration
object Google extends OauthProvider {

	@ConfigurationValue(name = "OAUTH.google.clientId", required = false)
	var clientId: String = _
	@ConfigurationValue(name = "OAUTH.google.clientSecret", required = false)
	var clientSecret: String = _

	private lazy val gson = new Gson()
	val isEnabled = Utils.nonEmpty(clientId) && Utils.nonEmpty(clientSecret)

	def authInfo(ctx: Context, redirectTo: String) = OauthInfo("google", createAuthUrl(ctx, redirectTo))

	lazy val service = new ServiceBuilder(clientId)
		.apiSecret(clientSecret)
		.defaultScope("email")
		.callback(s"http://${Settings.getHostname}${
			if (Settings.getPort != 80) {
				s":${Settings.getPort}"
			}
		}/admin/login/oauth?provider=${this.getClass.getSimpleName}")
		.build(GoogleApi20.instance)

	def createAuthUrl(ctx: Context, redirectTo: String): String = {
		if (!isEnabled) return ""

		val secretState = getSecretState(ctx)


		// Obtain the Authorization URL// Obtain the Authorization URL

		//pass access_type=offline to get refresh token
		//https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
		val additionalParams = new java.util.HashMap[String, String]
		additionalParams.put("access_type", "offline")
		//force to reget refresh token (if usera are asked not the first time)
		additionalParams.put("prompt", "consent")
		if (redirectTo != null) additionalParams.put(OauthProvider.REDIRECT_TO, redirectTo)
		val authorizationUrl: String = service.createAuthorizationUrlBuilder.state(secretState).additionalParams(additionalParams).build
		authorizationUrl
	}

	// TODO: endpoint discovery https://stackoverflow.com/questions/55541686/google-oauth2-userinfo-api-not-returning-users-name-data
	override def handleAuthResponse(ctx: Context)(implicit conn: Connection): Boolean = {
		val secret = ctx.queryParam("secretState")
		val code = ctx.queryParam("code")
		if (secret.isNullOrEmpty || code.isNullOrEmpty) {
			return false
		}

		var accessToken: OAuth2AccessToken = service.getAccessToken(code)

		val re = new OAuthRequest(Verb.GET, "https://openidconnect.googleapis.com/v1/userinfo?email")
		service.signRequest(accessToken, re)

		val response = service.execute(re)
		if (200 == response.getCode) {
			System.out.println(response.getBody)
			val authInfo = gson.fromJson(response.getBody, classOf[util.Map[String, String]]).asScala.toMap
			val email = authInfo("email")
			if (email.nonEmpty) {
				logIn(ctx, email)
			} else {
				false
			}
		} else {
			false
		}
	}
}