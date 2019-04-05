package cz.kamenitxan.jakon.utils.security.oauth

import com.github.scribejava.apis.GoogleApi20
import com.github.scribejava.core.builder.ServiceBuilder
import cz.kamenitxan.jakon.core.configuration.{Configuration, ConfigurationValue, Settings}
import cz.kamenitxan.jakon.utils.Utils
import spark.Request

import scala.util.Random


@Configuration
object Google extends OauthProvider{

	@ConfigurationValue(name = "OAUTH.google.clientId", required = false)
	var clientId: String = _
	@ConfigurationValue(name = "OAUTH.google.clientSecret", required = false)
	var clientSecret: String = _

	lazy val isEnabled = Utils.nonEmpty(clientId) && Utils.nonEmpty(clientSecret)
	def authInfo(req: Request) = OauthInfo("google/btn_google_signin_dark_normal_web@2x.png", createAuthUrl(req))

	def createAuthUrl(req: Request): String = {
		if (!isEnabled) return ""

		val secretState = this.getClass.getSimpleName+new Random().nextInt(99999)
		req.session().attribute(secretState)

		val service = new ServiceBuilder(clientId)
		  .apiSecret(clientSecret)
		  .defaultScope("email")
		  .callback(s"http://${Settings.hostname}${if (Settings.getPort != 80) {s":${Settings.getPort}"}}/oauth?provider=${this.getClass.getSimpleName}")
		  .build(GoogleApi20.instance)

		// Obtain the Authorization URL// Obtain the Authorization URL

		//pass access_type=offline to get refresh token
		//https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
		val additionalParams = new java.util.HashMap[String, String]
		additionalParams.put("access_type", "offline")
		//force to reget refresh token (if usera are asked not the first time)
		additionalParams.put("prompt", "consent")
		val authorizationUrl: String = service.createAuthorizationUrlBuilder.state(secretState).additionalParams(additionalParams).build
		authorizationUrl
	}
}